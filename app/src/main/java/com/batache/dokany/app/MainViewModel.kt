package com.batache.dokany.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val productsRepository = application.productsRepository
  private val sellersRepository = application.sellersRepository

  init {
    refreshProductsAndSellers()
  }

  private fun refreshProductsAndSellers() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          productsRepository.refreshProducts()
          sellersRepository.refreshSellers()
          _eventNetworkError.postValue(false)
        } catch (networkError: IOException) {
          // Show a Toast error message and hide the progress bar.
          _eventNetworkError.postValue(true)
        }
      }
    }
  }

  class Factory(private val application: DokanyApplication) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(application) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}