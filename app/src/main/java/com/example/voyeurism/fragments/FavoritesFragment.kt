package com.example.voyeurism.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.voyeurism.R
import com.example.voyeurism.adapters.ChaturbateAdapter
import com.example.voyeurism.viewmodels.FavoritesViewModel
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.material.progressindicator.LinearProgressIndicator
import retrofit2.HttpException

class FavoritesFragment : Fragment(), SearchView.OnQueryTextListener {

    // viewModel
    private val viewModel by lazy { ViewModelProvider(this)[FavoritesViewModel::class.java] }

    // viewControls
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChaturbateAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // searchView Controls-
    private lateinit var searchView: SearchView
    private lateinit var queryTextListener: SearchView.OnQueryTextListener
    private lateinit var searchMenuItem: MenuItem

    // loaderControls
    private lateinit var loaderProgressBar: ProgressBar
    private lateinit var loaderButton: Button
    private lateinit var loaderTextView: TextView

    // newInstance
    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }

    // onCreate´s
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        adapter = ChaturbateAdapter()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_chaturbate, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)!!
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)!!
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_dark)
        loaderProgressBar = view.findViewById(R.id.loader_progressBar)!!
        loaderButton = view.findViewById(R.id.loader_button)!!
        loaderTextView = view.findViewById(R.id.loader_textView)!!
        if (isConntectionAvailable()){ getFavorites() }else{ connectionLost() }
        setupRecyclerView()
        //recyclerView.refreshDrawableState()
        swipeToRefresh()
        getFavorites()
        return view
    }

    // getFavorites
    private fun getFavorites(){
        viewModel.fetchData(requireContext()).observe(viewLifecycleOwner) {
            try {
                recyclerView.adapter = adapter.apply {
                    addModels(it)
                    loaderProgressBar.visibility = View.GONE
                    loaderTextView.visibility = View.GONE
                    loaderButton.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    }
                if (it.isEmpty()){ empty() }
                if (recyclerView.isEmpty()) { empty() }
            } catch (e: HttpException) {
                error(e.message())
            }
        }
    }

    // swipeToRefresh
    private fun swipeToRefresh() {
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_dark)
        swipeRefreshLayout.setOnRefreshListener {
            recyclerView.adapter.apply {
                adapter.clearAll()
                getFavorites()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    // empty´s, errors, connectionLost
    @SuppressLint("SetTextI18n")
    private fun empty() {
        loaderProgressBar.visibility = View.GONE
        loaderTextView.visibility = View.VISIBLE
        loaderTextView.text = "no favorites found!"
        recyclerView.visibility = View.VISIBLE
        loaderButton.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun connectionLost(){
        loaderProgressBar.visibility = View.GONE
        loaderTextView.visibility = View.VISIBLE
        loaderTextView.text = "no internet connection"
        recyclerView.visibility = View.GONE
        loaderButton.visibility = View.VISIBLE
        loaderButton.setOnClickListener {
            getFavorites()
        }
        writeToast("no internet connection")
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

    // recyclerView Setup
    private fun setupRecyclerView(){
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }

    // utilitys --> internetCheck, writeToast
    private fun isConntectionAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting)
    }

    private fun writeToast(message: String) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show() }

    // searchView
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        CastButtonFactory.setUpMediaRouteButton(this.requireContext(), menu, R.id.media_route_menu_item)
        val searchItem = menu.findItem(R.id.app_bar_search)
        searchView = (searchItem.actionView as SearchView?)!!
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

}