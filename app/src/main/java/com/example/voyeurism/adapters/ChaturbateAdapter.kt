package com.example.voyeurism.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.voyeurism.R
import com.example.voyeurism.activitys.DetailsActivity
import com.example.voyeurism.favorites.Favorite
import com.example.voyeurism.models.ChaturbateModel
import com.example.voyeurism.reposetories.Chaturbate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern


class ChaturbateAdapter: RecyclerView.Adapter<ChaturbateAdapter.ViewHolder>(), Filterable {
    private lateinit var context: Context
    private var imageUrl:String = ""
    private var items: MutableList<ChaturbateModel> = mutableListOf()
    private var unfilteredItems:MutableList<ChaturbateModel> = mutableListOf()
    private var filteredItems:MutableList<ChaturbateModel> = mutableListOf()
    private var favoritesItems:MutableList<ChaturbateModel> = mutableListOf()
    private val favorite:Favorite = Favorite()
    private lateinit var holders:ViewHolder
    // Highlight Text
   // private lateinit var roomSubject: TextView

    init {
        this.unfilteredItems = this.items
        this.filteredItems = this.items
    }

    lateinit var listener : WorkoutClickLisetner

    interface WorkoutClickLisetner{
        fun onWorkoutClicked(hashTag: String)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener  {
        //val location:String,
        //val current_show:Boolean,
        //val tags:String,
        //val num_users: Int,
        //val num_followers:Int,
        //val spoken_languages:String,
        //val display_name:String,
        //val birthday:String,
        //val image_url_360x270:String,
        //val chat_room_url_revshare:String,
        //val iframe_embed_revshare:String,
        //val chat_room_url:String,
        //val iframe_embed:String,
        //val block_from_countries:String,
        //val block_from_states:String,
        //val recorded:Boolean,
        //val slug:Boolean
        private val imageUrl: String = ""
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var repo = Chaturbate()
        val gender: ImageView = itemView.findViewById(R.id.iv_gender)
        val username: TextView = itemView.findViewById(R.id.tv_name)
        var roomSubject: TextView = itemView.findViewById(R.id.tv_roomSubject)
        val isNew: TextView = itemView.findViewById(R.id.tv_label)
        val isHd: TextView = itemView.findViewById(R.id.tv_hd)
        val age: TextView = itemView.findViewById(R.id.tv_age)
        val secondsOnline: TextView = itemView.findViewById(R.id.tv_secondsOnline)
        val image: ImageView = itemView.findViewById(R.id.iv_image)
        val progressBar: ProgressBar = itemView.findViewById(R.id.pb_progressbar)
        val modelCheckBox: AppCompatCheckBox = itemView.findViewById(R.id.favorite_check)
        val modelTags: TextView = itemView.findViewById(R.id.tv_tags)
        val modelLink: TextView = itemView.findViewById(R.id.tv_link)
        init { itemView.setOnClickListener(this) }
        override fun onClick(v: View?) {
            if (this.modelLink.text.contains("chaturbate")) {
                repo = Chaturbate()
                var videoLink: String
                scope.launch {
                    try {
                        videoLink = repo.parseVideo(modelLink.text.toString())
                        withContext(Dispatchers.Main) {
                            val intent = Intent(v!!.context, DetailsActivity::class.java)
                            intent.putExtra("modelName", username.text)
                            intent.putExtra("modelImage", this@ViewHolder.imageUrl)
                            intent.putExtra("modelImageUrl", this@ViewHolder.image.toString())
                            intent.putExtra("modelM3u8", videoLink)
                            Toast.makeText(itemView.context, "Open " + username.text.toString().uppercase() + "´s" + " Cam", Toast.LENGTH_SHORT).show()
                            v.context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(itemView.context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(LayoutInflater.from(parent.context).inflate (
                    R.layout.recyclerview_chaturbate,
                    parent,
                    false
        )
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thisModel = filteredItems[position]
        holders = holder
        imageUrl = thisModel.image_url
        holder.username.text = thisModel.username
        Glide.with(holder.itemView).load(thisModel.image_url).error(R.drawable.chaturbate_nopicture)
            .dontTransform()
            .into(holder.image)
        holder.image.visibility = View.VISIBLE
        holder.progressBar.visibility = View.GONE
        Log.d("", holder.image.toString())
        //holder.modelDescription.text = thisModel.Description  -> UNTENDRUNTER IMPLAMENTIERT!
        val ss = SpannableString(thisModel.room_subject)
        val words: List<String> = ss.split(" ")
        for (word: String in words) {
            if (word.startsWith("#")) {
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        listener.onWorkoutClicked(word)
                    }
                }
                ss.setSpan(
                    clickableSpan,
                    ss.indexOf(word),
                    ss.indexOf(word) + word.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        holder.roomSubject.apply {
            text = ss
            movementMethod = LinkMovementMethod.getInstance()
        }
        val online = secondsToHoursAndMinutes(thisModel.seconds_online)
        val users = thisModel.num_users
        val viewersFinished = "$online, $users viewers"
        holder.secondsOnline.text = viewersFinished
        // Gender Icon Chances --->
        if (thisModel.gender == "f") {  //Female
            Glide.with(holder.itemView).load(R.drawable.ico_female)
                .dontTransform()
                .into(holder.gender)
        } else if (thisModel.gender == "s") { //Transe
            Glide.with(holder.itemView).load(R.drawable.ico_trans)
                .dontTransform()
                .into(holder.gender)
        } else if (thisModel.gender == "m") { //Male
            Glide.with(holder.itemView).load(R.drawable.ico_male)
                .dontTransform()
                .into(holder.gender)
        } else if (thisModel.gender == "c") { //Couple
            Glide.with(holder.itemView).load(R.drawable.ico_couple)
                .dontTransform()
                .into(holder.gender)
        }
        // AGE --->
        if (thisModel.age == 0){
            holder.age.visibility = View.GONE
            holder.age.text = ""
        } else {
            holder.age.text = thisModel.age.toString().replace("null","")
        }
        // LABELS --->
        if (thisModel.is_new){
            holder.isNew.visibility = View.VISIBLE
            holder.isHd.visibility = View.GONE
        }else if(thisModel.is_hd){
            holder.isHd.visibility = View.VISIBLE
            holder.isNew.visibility = View.GONE
        } else{
            holder.isHd.visibility = View.GONE
            holder.isNew.visibility = View.GONE
        }
        //holder.modelTags.text = thisModel.tags.toString()
        Log.d("ADAPTER -TAGS: ", thisModel.tags.toString())
        // Link --->
        holder.modelLink.text = thisModel.link
        // Model --->
        val model:ChaturbateModel = ChaturbateModel(thisModel.gender, "", true, thisModel.username, thisModel.room_subject, arrayListOf(), thisModel.is_new,thisModel.num_users, thisModel.num_followers, "", "", "", thisModel.is_hd, thisModel.age,thisModel.age,thisModel.image_url,thisModel.image_url,"","","","","","",false, false, thisModel.link)
        // FAVORITE CHECKBOX SELECTION --->
        val favorite:Favorite = Favorite()
        //favoritesItems = favorite.getFavorites(context)!!
        holder.modelCheckBox.isChecked = checkFavoriteItem(thisModel)
        holder.modelCheckBox.isChecked = favoritesItems.contains(model)
        holder.modelCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // if (checkFavoriteItem(model)) {
                if (isChecked) { //checkbox hecked
                    favorite.addFavorite(context, thisModel)
                    if(checkFavoriteItem(thisModel)){ favoritesItems.add(thisModel) }
                    //favoritesItems.add(model)
                    //holder.modelCheckBox.background(R.drawable.following)
                    holder.modelCheckBox.isChecked = true
                    writeToast(model.username + " saved in Favorites")
                } else { //checkbox unchecked
                    favorite.removeFavorite(context, thisModel)
                    if(checkFavoriteItem(thisModel)){ favoritesItems.add(thisModel) }
                    //favoritesItems.remove(model)
                    holder.modelCheckBox.isChecked = false
                    writeToast(model.username + " removed from Favorites")
                }
                    //holder.modelCheckBox.isChecked == isChecked
                //favoritesItems.remove(model)
        }
        // Tags --->
        for(tag in thisModel.tags){
            model.tags.add(tag)
            holder.modelTags.text = tag
        }
        // Text Highlight
        // Save Favorite List
        holder.modelCheckBox.isChecked = checkFavoriteItem(thisModel)
        favorite.saveFavorite(context, favoritesItems)
    }

    // timer

    fun scheduleWithCoroutine() {
        val timer = object: CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //writeToast(millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {

                writeToast("Done!")
            }
        }

        timer.start()
    }

    // check Favorites

    private fun checkFavoriteItem(model: ChaturbateModel): Boolean {
        var check = false
        val favorites: MutableList<ChaturbateModel>? = favorite.getFavorites(context)
        if (favorites != null) {
            for (item in favorites) {
                if (item == model) {
                    check = true
                    break
                }
            }
        }
        return check
    }

    override fun getItemCount(): Int { return filteredItems.size }

    override fun getFilter(): Filter {
        return object : Filter() {
            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredItems = filterResults.values as ArrayList<ChaturbateModel>
                notifyDataSetChanged()
            }
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val queryString: String = charSequence.toString().lowercase(Locale.ROOT)
                val filterResults = FilterResults()
                filterResults.values = if (queryString.isEmpty()) { filteredItems = items
                } else {
                    filterResults.values = genderSelection(queryString)
                    filterResults.values = searchSelection(queryString)
                }
                // filterResults.values = filteredItems
                return filterResults
            } }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun searchSelection(queryString: String){
        val filteredList: ArrayList<ChaturbateModel> = ArrayList()
        items.let { for (model in it) {
            try {
                if (model.username.contains(queryString)) {
                    filteredList.add(model)
                    val yellowPaint = Paint().apply {
                        color = Color.YELLOW
                    }
                    setSearchTextHighlightSimpleSpannable(holders.username,model.username, queryString)
                    setHighLightedText(holders.username, queryString)
                }else if(model.tags.toString().lowercase(Locale.ROOT).contains("#" + queryString.lowercase(Locale.ROOT))) {
                    filteredList.add(model)
                    setSearchTextHighlightSimpleSpannable(holders.roomSubject,model.room_subject, queryString.lowercase(Locale.ROOT))
                    setHighLightedText(holders.roomSubject, queryString)
                }
            }catch (ex:Exception){}
        }
        if (filteredList.isNotEmpty()) { filteredItems = filteredList } else { Log.d("Error Message:", " no Search Found! ") } }
    }

    fun genderSelection(queryString: String){
        val filteredList: ArrayList<ChaturbateModel> = ArrayList()
        items.let { for (model in it) {
            if (queryString == "Transe") {
                if (model.gender == "s") { filteredList.add(model) }
            } else if (queryString == "Male") {
                if (model.gender == "m") { filteredList.add(model) }
            } else if (queryString == "Female") {
                if (model.gender == "f") { filteredList.add(model) }
            } else if (queryString == "Couple") {
                if (model.gender == "c") { filteredList.add(model) }
            } else if (queryString == "Featured") {
                filteredList.add(model)
            } }
        }
        if (filteredList.isNotEmpty()) { filteredItems = filteredList } else { Log.d("Error Message:", " no Gender Found! ") }
    }

    //Multiplikation & Substration!

    fun getModels():MutableList<ChaturbateModel>{
        return filteredItems
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addMore(data: MutableList<ChaturbateModel>){
        items.addAll(data)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun add(model: ChaturbateModel){
        items.add(model)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addModels(items: List<ChaturbateModel>) {
        this.items.apply {
            clear()
            addAll(items)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearAll(){
        items.removeAll { true }
        filteredItems.removeAll { true }
        unfilteredItems.removeAll { true }
        notifyDataSetChanged()
    }

    //Support -->

    private fun secondsToHoursAndMinutes(seconds:Int):String {
        val min: Int = seconds / 3600
        val hours: Int = seconds % 3600 / 60
        val strmin = if (min < 10) "0$min" else min.toString()
        val strHours = if (hours < 10) "0$hours" else hours.toString()
        return if (strHours <= "1")  { "$strmin mins" } else { "$strHours hrs" }
    }

    private fun writeToast(message: String) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }

    // Highlight Text


    private fun setHighLightedText(tv: TextView, textToHighlight: String) {
        val tvt = tv.getText().toString()
        var ofe = tvt.indexOf(textToHighlight, 0)
        val wordToSpan: Spannable = SpannableString(tv.getText())
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToHighlight, ofs)
            if (ofe == -1) break else {
                // set color here
                wordToSpan.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    ofe,
                    ofe + textToHighlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }

    private fun setSearchTextHighlightSimpleSpannable(textView: TextView, fullText: String?, searchText: String?) {
        if (!searchText.isNullOrEmpty()) {
            val wordSpan = SpannableStringBuilder(fullText)
            val p: Pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE)
            val m: Matcher = p.matcher(fullText!!)
            while (m.find()) {
                val wordStart = m.start()
                val wordEnd = m.end()
                // Now highlight based on the word boundaries
                val redColor = ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.BLACK))
                val highlightSpan = TextAppearanceSpan(null, Typeface.BOLD, -1, redColor, null)
                wordSpan.setSpan(
                    highlightSpan,
                    wordStart,
                    wordEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                wordSpan.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    wordStart,
                    wordEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                wordSpan.setSpan(
                    RelativeSizeSpan(1.25f),
                    wordStart,
                    wordEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            textView.setText(wordSpan, TextView.BufferType.SPANNABLE)
        } else {
            textView.text = fullText
        }
    }

}