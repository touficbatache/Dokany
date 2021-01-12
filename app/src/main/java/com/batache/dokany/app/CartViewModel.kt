package com.batache.dokany.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batache.dokany.db.cart_products.CartProductsRepository
import com.batache.dokany.model.pojo.*
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartProductsRepository) : ViewModel() {

  val cartProductsDetails: LiveData<List<CartProductDetails>> = repository.cartProductsDetails

  fun insert(cartProduct: CartProduct) {
    viewModelScope.launch {
      repository.insert(cartProduct)
    }
  }

}

class CartViewModelFactory(private val repository: CartProductsRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return CartViewModel(
        repository
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}