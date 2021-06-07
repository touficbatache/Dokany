package com.batache.dokany.app.cart.checkout

import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.api.FirebaseRepository
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.model.pojo.Address
import com.batache.dokany.model.pojo.cart.CartProduct
import kotlinx.coroutines.launch

class CheckoutViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val repository = application.cartProductsRepository

  /**
   * Live data.
   */
  private val _checkoutInformationState: MediatorLiveData<CheckoutInformationState> =
    MediatorLiveData()
  val checkoutInformationState: LiveData<CheckoutInformationState>
    get() = _checkoutInformationState

  init {
    _checkoutInformationState.value =
      CheckoutInformationState(
        addresses = ArrayList(),
        isDeliveryAddressChosen = false,
        isDeliveryTimeChosen = false,
        cartProducts = ArrayList(),
        isOrderPlaced = null,
      )
  }

  fun getCurrentUserAddresses() {
    viewModelScope.launch {
      val userAddresses = firebaseRepo.getCurrentUserAddresses()
      setAddresses(userAddresses)
    }
  }

  fun getCartProducts() {
    viewModelScope.launch {
      val cartProducts = repository.getCartProducts()
      setCartProducts(cartProducts)
    }
  }

  private fun setAddresses(addresses: List<Address>) {
    _checkoutInformationState.postValue(
      _checkoutInformationState.value?.copy(
        addresses = addresses
      )
    )
  }

  fun setDeliveryAddressChosen(isChosen: Boolean) {
    _checkoutInformationState.postValue(
      _checkoutInformationState.value?.copy(
        isDeliveryAddressChosen = isChosen
      )
    )
  }

  fun setDeliveryTimeChosen(isChosen: Boolean) {
    _checkoutInformationState.postValue(
      _checkoutInformationState.value?.copy(
        isDeliveryTimeChosen = isChosen
      )
    )
  }

  private fun setCartProducts(cartProducts: List<CartProduct>) {
    _checkoutInformationState.postValue(
      _checkoutInformationState.value?.copy(
        cartProducts = cartProducts
      )
    )
  }

  fun placeOrder(
    deliveryAddress: String,
    deliveryTime: String,
    additionalInfo: String,
    allowSubstitutions: Boolean
  ) {
    viewModelScope.launch {
      val isSuccessful = firebaseRepo.placeOrder(
        FirebaseRepository.FirestoreOrder(
          repository.getCartProductsJoin(), deliveryAddress, deliveryTime, additionalInfo, allowSubstitutions
        )
      )
      setOrderPlaced(isSuccessful)
    }
  }

  fun setOrderPlaced(isPlaced: Boolean) {
    _checkoutInformationState.postValue(
      _checkoutInformationState.value?.copy(
        isOrderPlaced = isPlaced
      )
    )
  }

  fun onOrderPlaced() {
    viewModelScope.launch {
      repository.deleteAll()
    }
  }

  data class CheckoutInformationState(
    val addresses: List<Address>,
    val isDeliveryAddressChosen: Boolean,
    val isDeliveryTimeChosen: Boolean,
    val cartProducts: List<CartProduct>,
    val isOrderPlaced: Boolean?,
  )

  companion object {
    const val TAG = "CheckoutViewModel"
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return CheckoutViewModel(app) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}