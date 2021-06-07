package com.batache.dokany.app.orders

import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.model.pojo.Order
import com.batache.dokany.model.pojo.cart.CartProductJoin
import kotlinx.coroutines.launch

class OrderDetailViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val repository = application.cartProductsRepository

  /**
   * Live data.
   */
  private var _order: MutableLiveData<Order?> = MutableLiveData()
  val order: LiveData<Order?>
    get() = _order

  fun getOrder(orderId: String) {
    viewModelScope.launch {
      _order.postValue(firebaseRepo.getOrder(orderId))
    }
  }

  fun setCartProducts(products: List<CartProductJoin>) {
    viewModelScope.launch {
      repository.deleteAll()
      repository.insertAll(products)
    }
  }

  companion object {
    const val TAG = "OrderDetailViewModel"
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return OrderDetailViewModel(app) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}