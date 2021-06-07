package com.batache.dokany.app.shop.products.product_details

import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.model.pojo.cart.CartProductJoin
import com.batache.dokany.model.pojo.product.Product
import kotlinx.coroutines.launch

class ProductDetailsViewModel(application: DokanyApplication, val productId: String) : BaseViewModel(application) {

  /**
   * The data sources this ViewModel will fetch results from.
   */
  private val productsRepository = application.productsRepository
  private val cartProductsRepository = application.cartProductsRepository

  /**
   * Live data.
   */
  val product = productsRepository.getProduct(productId)
//  val productSellers = productSellersRepository.getProductSellers(productId)
  val relatedProducts = productsRepository.products

  var quantityInCart: Int = 0
  var cartSellerId: String? = null

  /**
   * Events
   */
  private val _cartEvent: MediatorLiveData<CartEvent> = MediatorLiveData()
  val cartEvent: LiveData<CartEvent>
    get() = _cartEvent

  init {
    refreshDataFromRepository()
    syncWithCart()
  }

//  suspend fun getRecommendedSellerProductDetails(): LiveData<SellerProductDetailz> {
//    return productsRepository.getRecommendedSellerProductDetails(productId)
//  }

  private fun refreshDataFromRepository() {
//    viewModelScope.launch {
//      try {
//        productSellersRepository.refreshSellersForProduct(productId)
//        x.value = false
//        _isNetworkErrorShown.value = false
//      } catch (networkError: Exception) {
//        // Show a Toast error message and hide the progress bar.
//        if (productSellers.value.isNullOrEmpty())
//          _eventNetworkError.value = true
//      }
//    }
  }

  fun addToFavorites() {
    addToFavorites(productId)
  }

  fun addToFavorites(productId: String) {
    viewModelScope.launch {
      if (!productsRepository.addToFavorites(productId)) {
        _eventAuthError.postValue(true)
      }
    }
  }

  fun isInFavorites(): LiveData<Boolean> {
    val liveData: MediatorLiveData<Boolean> = MediatorLiveData()

    viewModelScope.launch {
      liveData.postValue(productsRepository.isInFavorites(productId))
    }

    return liveData
  }

  fun isInFavoritesLive(): LiveData<Product?> = productsRepository.isInFavoritesLive(productId).asLiveData()

  fun removeFromFavorites() {
    viewModelScope.launch {
      productsRepository.removeFromFavorites(productId)
    }
  }

  private fun syncWithCart() {
    viewModelScope.launch {
      val sellerId = cartProductsRepository.getSellerId(productId)
      sellerId?.let {
        cartSellerId = sellerId
      }

      val quantity = cartProductsRepository.getQuantity(productId)
      quantity?.let {
        quantityInCart = quantity
      }

      _cartEvent.postValue(
        CartEvent.SyncComplete(
          quantityInCart > 0
        )
      )
    }
  }

  fun addToCart(sellerId: String?, quantity: Int) {
    sellerId?.let {
      viewModelScope.launch {
        cartProductsRepository.insert(
          CartProductJoin(
            productId = productId,
            sellerId = sellerId,
            quantity = quantity
          )
        )
        quantityInCart = quantity
        _cartEvent.postValue(CartEvent.AddedToCart)
      }
    }
  }

  fun updateCartQuantity(quantity: Int) {
    viewModelScope.launch {
      cartProductsRepository.updateQuantity(productId, quantity)
      quantityInCart = quantity
      _cartEvent.postValue(CartEvent.QuantityChanged)
    }
  }

  fun removeFromCart() {
    viewModelScope.launch {
      cartProductsRepository.delete(productId)
      quantityInCart = 0
      _cartEvent.postValue(CartEvent.RemovedFromCart)
    }
  }

  fun onCartQuantityModified() {
    _cartEvent.postValue(CartEvent.QuantityModified)
  }

  sealed class CartEvent {
    data class SyncComplete(val isInCart: Boolean) : CartEvent()
    object AddedToCart : CartEvent()
    object RemovedFromCart : CartEvent()
    object QuantityModified : CartEvent()
    object QuantityChanged : CartEvent()
  }

  /**
   * Resets the network error flag.
   */
  fun onNetworkErrorShown() {
    _isNetworkErrorShown.value = true
  }

  /**
   * Factory for constructing ProductDetailViewModel with parameter
   */
  class Factory(val app: DokanyApplication, val productId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(ProductDetailsViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return ProductDetailsViewModel(app, productId) as T
      }
      throw IllegalArgumentException("Unable to construct ViewModel")
    }
  }
}