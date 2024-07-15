package com.example.voyeurism.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.room.util.query
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.voyeurism.R
import com.example.voyeurism.activitys.MainActivity
import com.example.voyeurism.adapters.ChaturbateAdapter
import com.example.voyeurism.viewmodels.ChaturbateViewModel
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.MainScope
import retrofit2.HttpException

private const val ARG_MODUS = "modus"
private const val ARG_PARAMETER = "parameter"
class ChaturbateFragment : Fragment() {
    // viewModel
    private val viewModel by lazy { ViewModelProvider(this)[ChaturbateViewModel::class.java] }

    // viewControls
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChaturbateAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // searchView Controls
    private lateinit var searchView: SearchView
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var searchMenuItem: MenuItem

    // loaderControls
    private lateinit var loaderProgressBar: ProgressBar
    private lateinit var loaderButton: Button
    private lateinit var loaderTextView: TextView

    // parametes ---> modus & parameter
    private var modus: String = "gender"
    private var parameter: String = "Featured"

    // newInstance
    companion object {
        @JvmStatic
        fun newInstance(modus: String, parameter: String) = ChaturbateFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODUS, modus)
                putString(ARG_PARAMETER, parameter)
            }
        }
    }

    // onCreate´s
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (arguments != null) {
            modus = requireArguments().getString(ARG_MODUS).toString()
            parameter = requireArguments().getString(ARG_PARAMETER).toString()
            if(requireArguments().containsKey("gender")){
                requireActivity().title = parameter
            } else if (requireArguments().containsKey("search")){
                requireActivity().title = "\"" + parameter + "\""
            }
        }
        adapter = ChaturbateAdapter()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chaturbate, container, false)
        loaderProgressBar = view.findViewById(R.id.loader_progressBar)!!
        loaderButton = view.findViewById(R.id.loader_button)!!
        loaderTextView = view.findViewById(R.id.loader_textView)!!
        recyclerView = view.findViewById(R.id.recyclerView)!!
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)!!
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_dark)
        if (isConntectionAvailable()){ parseContent() }else{ connectionLost() }
        setupRecyclerView()
        swipeToRefresh()
        parseContent()
        return view
    }

    // parseContent
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun parseContent(){
        viewModel.fetchData().observe(viewLifecycleOwner) {
            try {
                if (recyclerView.isEmpty()) {
                    empty()
                    recyclerView.adapter = adapter.apply {
                        addModels(it)
                        if (modus.contains("gender")){
                            genderSelection(parameter)
                        }
                        if (modus.contains("search")){
                            searchSelection(parameter)
                        }
                    }
                    successfull()
                }
            }catch (e:HttpException){
                error(e.message())
            }
        }
    }

    // swipeToRefresh
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun swipeToRefresh() {
        loaderProgressBar.visibility = View.VISIBLE
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_dark)
        swipeRefreshLayout.setOnRefreshListener {
            recyclerView.adapter.apply {
                adapter.clearAll()
                parseContent()
            }
            swipeRefreshLayout.isRefreshing = false
            loaderProgressBar.visibility = View.GONE

        }
    }

    // succcesfull´s, errors, emptys, connectionLost
    @SuppressLint("SetTextI18n")
    private fun successfull(){
        if (recyclerView.isEmpty()) {
            loaderButton.visibility = View.GONE
            loaderProgressBar.visibility = View.GONE
            loaderTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }else{
            loaderButton.visibility = View.GONE
            loaderProgressBar.visibility = View.GONE
            loaderTextView.visibility = View.VISIBLE
            loaderTextView.setTextColor(Color.RED)
            loaderTextView.text = "error load content"
            recyclerView.visibility = View.GONE
        }
    }

    private fun error(msg:String){
        loaderProgressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        loaderTextView.visibility = View.VISIBLE
        loaderTextView.setTextColor(Color.RED)
        loaderTextView.text = msg
        loaderButton.visibility = View.VISIBLE
        writeToast("Error: $msg")
    }

    @SuppressLint("SetTextI18n")
    private fun empty(){
        loaderProgressBar.visibility = View.GONE
        loaderTextView.visibility = View.VISIBLE
        loaderTextView.setTextColor(Color.RED)
        loaderTextView.text = "no models found"
        recyclerView.visibility = View.GONE
        loaderButton.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("SetTextI18n")
    private fun connectionLost(){
        loaderProgressBar.visibility = View.GONE
        loaderTextView.visibility = View.VISIBLE
        loaderTextView.setTextColor(Color.RED)
        loaderTextView.text = "no internet connection"
        recyclerView.visibility = View.GONE
        loaderButton.visibility = View.VISIBLE
        loaderButton.setOnClickListener {
            parseContent()
        }
        writeToast("no internet connection")
    }

    // utilitys --> internetCheck, writeToast
    private fun isConntectionAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting)
    }

    private fun writeToast(message: String) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show() }

    // recyclerView Setup
    private fun setupRecyclerView(){
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }

    // searchView
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        CastButtonFactory.setUpMediaRouteButton(this.requireContext(), menu, R.id.media_route_menu_item)
        val searchItem = menu.findItem(R.id.app_bar_search)
        searchView = (searchItem.actionView as SearchView?)!!
        searchView.queryHint = "Suche.."
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener {
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun onQueryTextSubmit(query: String?): Boolean {
                MenuItemCompat.collapseActionView(searchMenuItem)
                viewModel.fetchData().observe(viewLifecycleOwner) {
                    try {
                        recyclerView.adapter = adapter.apply {
                            addModels(it)
                            searchSelection(query!!)
                        }
                    }catch (e:HttpException){
                        error(e.message())
                    }
                }
                Toast.makeText(requireContext(), query, Toast.LENGTH_SHORT).show()
            return true
            }
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.fetchData().observe(viewLifecycleOwner) {
                    try {
                        if(newText?.length!! >= 2) {
                        recyclerView.adapter = adapter.apply {
                            addModels(it)
                            searchSelection(newText)
                        } }
                    }catch (e:HttpException){
                        error(e.message())
                    }
                }
                Toast.makeText(requireContext(), newText, Toast.LENGTH_SHORT).show()
            return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

}