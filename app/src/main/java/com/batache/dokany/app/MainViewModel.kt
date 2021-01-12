package com.batache.dokany.app

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batache.dokany.db.product_sellers.ProductSellersRepository
import com.batache.dokany.db.sellers.SellersRepository
import com.batache.dokany.model.pojo.ViewModelConnection
import kotlinx.coroutines.launch
import java.io.IOException

class MainViewModel(private val sellersRepository: SellersRepository) : ViewModel() {

  val connectionLiveData: MediatorLiveData<ViewModelConnection> = MediatorLiveData()

  init {
//    refreshSellersDataIfNeeded()
    refreshSellersFromRepository()
  }

  private fun refreshSellersDataIfNeeded() {
    viewModelScope.launch {
      if (sellersRepository.getLocalSellersCount() <= 0) {
        refreshSellersFromRepository()
      }
    }
  }

  private fun refreshSellersFromRepository() {
    viewModelScope.launch {
      try {
        sellersRepository.refreshSellers()
        connectionLiveData.postValue(
          ViewModelConnection(
            eventNetworkError = false
          )
        )

      } catch (networkError: IOException) {
        // Show a Toast error message and hide the progress bar.
        connectionLiveData.postValue(
          ViewModelConnection(
            eventNetworkError = true
          )
        )
      }
    }
  }

}

class MainViewModelFactory(private val sellersRepository: SellersRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return MainViewModel(sellersRepository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}