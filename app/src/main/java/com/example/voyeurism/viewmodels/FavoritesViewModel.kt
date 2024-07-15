package com.example.voyeurism.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyeurism.favorites.Favorite
import com.example.voyeurism.models.ChaturbateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private var items: MutableLiveData<MutableList<ChaturbateModel>> = MutableLiveData()
    private val repo = Favorite()

    fun fetchData(context: Context): MutableLiveData<MutableList<ChaturbateModel>> {
        viewModelScope.launch(Dispatchers.IO) {
            items.postValue(repo.getFavorites(context))
        }
        return items
    }

}