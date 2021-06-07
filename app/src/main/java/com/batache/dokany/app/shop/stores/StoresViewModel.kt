package com.batache.dokany.app.shop.stores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.db.sellers.SellersRepository
import kotlinx.coroutines.launch
import java.io.IOException

class StoresViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val sellersRepository: SellersRepository = application.sellersRepository

  /**
   * Live data.
   */
  val sellers = sellersRepository.sellersLive

  init {
    refreshDataFromRepository()
  }

  private fun refreshDataFromRepository() {
    viewModelScope.launch {
      try {
        sellersRepository.refreshSellers()
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
    const val TAG = "StoresViewModel"
  }

  class Factory(private val application: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(StoresViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return StoresViewModel(application) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}