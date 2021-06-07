package com.batache.dokany.app.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.model.pojo.cart.CartProduct
import com.batache.dokany.model.pojo.cart.CartProductJoin
import kotlinx.coroutines.launch

class CartViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val repository = application.cartProductsRepository

  /**
   * Live data.
   */
  val cartProducts: LiveData<List<CartProduct>> = repository.cartProductsLive

  fun insert(cartProduct: CartProductJoin) {
    viewModelScope.launch {
      repository.insert(cartProduct)
    }
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return CartViewModel(app) as T
      }
      throw IllegalArgumentException("Unable to construct ViewModel")
    }
  }
}