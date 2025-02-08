package com.example.ssul.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ssul.model.FavoriteModel

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val _favoriteState = MutableLiveData<List<FavoriteModel>>()
    val favoriteState: LiveData<List<FavoriteModel>> get() = _favoriteState

    init {
        loadFavorites()
    }

    // 즐겨찾기 상태 불러오기
    private fun loadFavorites() {
        val prefs = getApplication<Application>().getSharedPreferences("favorite", Context.MODE_PRIVATE)
        val favorites = mutableListOf<FavoriteModel>()

        prefs.all.forEach { (key, value) ->
            if (value is Boolean) {
                favorites.add(FavoriteModel(storeId = key.toInt(), isFavorite = value))
            }
        }
        _favoriteState.value = favorites
    }

    // 즐겨찾기 토글 기능
    fun toggleFavorite(storeId: Int) {
        val currentList = _favoriteState.value?.toMutableList() ?: mutableListOf()

        val selectedItem: FavoriteModel? = currentList.find { it.storeId == storeId }
        if (selectedItem != null) {
            selectedItem.isFavorite = !selectedItem.isFavorite
        } else {
            currentList.add(FavoriteModel(storeId, true))
        }

        _favoriteState.value = currentList
        saveFavoriteState(storeId, selectedItem?.isFavorite ?: true)
    }

    // 즐겨찾기 상태 저장
    private fun saveFavoriteState(storeId: Int, isFavorite: Boolean) {
        val prefs = getApplication<Application>().getSharedPreferences("favorite", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(storeId.toString(), isFavorite)
            apply()
        }
    }

}