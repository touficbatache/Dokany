package com.batache.dokany.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batache.dokany.db.products.ProductsRepository
import kotlinx.coroutines.launch
import java.io.IOException

class ShopViewModel(private val repository: ProductsRepository) : ViewModel() {

  val products = repository.products

  init {
    refreshDataFromRepository()
  }

  private fun refreshDataFromRepository() {
    viewModelScope.launch {
      try {
        repository.refreshProducts()
//        _eventNetworkError.value = false
//        _isNetworkErrorShown.value = false

      } catch (networkError: IOException) {
        // Show a Toast error message and hide the progress bar.
//        if (playlist.value.isNullOrEmpty())
//          _eventNetworkError.value = true
      }
    }
  }

  companion object {
    const val TAG = "ShopViewModel"
  }

}

class ShopViewModelFactory(private val repository: ProductsRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return ShopViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}