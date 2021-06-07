package com.batache.dokany.app.shop.stores

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.batache.dokany.DokanyApplication
import com.batache.dokany.model.pojo.Seller

class StoreDetailsViewModel(application: DokanyApplication, sellerId: String) :
  AndroidViewModel(application) {

  /**
   * The data source this ViewModel will fetch results from.
   */
  private val sellersRepository = application.sellersRepository

  /**
   * Live data.
   */
  val sellerDetails: LiveData<Seller> = sellersRepository.getSeller(sellerId)

  class Factory(val app: DokanyApplication, val sellerId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(StoreDetailsViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return StoreDetailsViewModel(app, sellerId) as T
      }
      throw IllegalArgumentException("Unable to construct ViewModel")
    }
  }
}