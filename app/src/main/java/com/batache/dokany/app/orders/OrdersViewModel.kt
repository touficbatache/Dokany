package com.batache.dokany.app.orders

import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.model.pojo.Order
import com.batache.dokany.model.pojo.cart.CartProductJoin
import kotlinx.coroutines.launch

class OrdersViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val repository = application.cartProductsRepository

  /**
   * Live data.
   */
  private var _orders: MutableLiveData<List<Order>> = MutableLiveData()
  val orders: LiveData<List<Order>>
    get() = _orders

  private var _isOrderPlaced: MutableLiveData<Boolean> = MutableLiveData()
  val isOrderPlaced: LiveData<Boolean>
    get() = _isOrderPlaced

  fun getOrders() {
    viewModelScope.launch {
      _orders.postValue(firebaseRepo.getOrders())
    }
  }

  companion object {
    const val TAG = "OrdersViewModel"
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return OrdersViewModel(app) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}