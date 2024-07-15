package com.example.voyeurism.favorites

import android.content.Context
import android.content.SharedPreferences
import com.example.voyeurism.activitys.MainActivity
import com.example.voyeurism.models.ChaturbateModel
import com.google.gson.Gson

private const val PREFS_NAME: String = "VOYEURISM_APP"
private const val FAVORITES: String = "FAVORITE"
class Favorite() {
    private val context: Context = MainActivity()

    // THIS FOUR METHODS ARE USED FOR MAINTAINING FAVORITES.
    fun saveFavorite(context: Context, modelList: List<ChaturbateModel>) {
        val newModelList:List<ChaturbateModel> = removeDuplicates(modelList.toMutableList())
        val settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        val gson = Gson()
        val jsonFavorites = gson.toJson(newModelList)
        editor.putString(FAVORITES, jsonFavorites)
        editor.apply()
    }
    fun addFavorite(context: Context, model: ChaturbateModel) {
        val favorites: MutableList<ChaturbateModel> = getFavorites(context)!!.toMutableList()
        favorites.add(model)
        saveFavorite(context, favorites)
    }
    fun removeFavorite(context: Context, model: ChaturbateModel?) {
        var favorites: ArrayList<ChaturbateModel> = getFavorites(context) as ArrayList<ChaturbateModel>
        favorites = ArrayList<ChaturbateModel>()
        favorites.remove(model)
        saveFavorite(context, favorites)
    }
    fun getFavorites(context: Context): MutableList<ChaturbateModel>? {
        var favorites: List<ChaturbateModel?>
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (settings.contains(FAVORITES)) {
            val jsonFavorites = settings.getString(FAVORITES, null)
            val gson = Gson()
            val favoriteItems: Array<ChaturbateModel> = gson.fromJson(
                jsonFavorites,
                Array<ChaturbateModel>::class.java
            )
            favorites = listOf(*favoriteItems)
            favorites = ArrayList<ChaturbateModel>(favorites)
        } else {
            return null
        }
        return favorites
    }
    private fun removeDuplicates(mutableList: MutableList<ChaturbateModel>):MutableList<ChaturbateModel>{
        val reportList:MutableList<ChaturbateModel> = mutableList
        for(model in mutableList){
            if(!reportList.contains(model)){
                reportList.add(model)
            }
        }
        return reportList
    }
}