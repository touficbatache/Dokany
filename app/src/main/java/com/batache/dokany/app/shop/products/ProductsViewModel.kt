package com.batache.dokany.app.shop.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ProductsViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val productsRepository = application.productsRepository

  /**
   * Live data.
   */
  val products = productsRepository.products

  private fun refreshProducts() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          productsRepository.refreshProducts()
          _eventNetworkError.postValue(false)
          _eventFirestoreSuccess.postValue(true)
        } catch (networkError: IOException) {
          // Show a Toast error message and hide the progress bar.
          _eventNetworkError.postValue(true)
          _eventFirestoreError.postValue(true)
        }
      }
    }
  }

  fun addToFavorites(productId: String) {
    viewModelScope.launch {
      if (!productsRepository.addToFavorites(productId)) {
        _eventAuthError.postValue(true)
      }
    }
  }

  companion object {
    const val TAG = "ProductsViewModel"
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return ProductsViewModel(app) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}

