package com.batache.dokany.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.batache.dokany.db.favorites.FavoritesRepository
import com.batache.dokany.model.pojo.Product

class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {

  val favoritedProducts: LiveData<List<Product>> = repository.allFavoritedProducts.asLiveData()
}

class FavoritesViewModelFactory(private val repository: FavoritesRepository) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return FavoritesViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}