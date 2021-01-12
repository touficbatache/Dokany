package com.batache.dokany.app

import androidx.lifecycle.*
import com.batache.dokany.db.cart_products.CartProductsRepository
import com.batache.dokany.db.favorites.FavoritesRepository
import com.batache.dokany.db.product_sellers.ProductSellersRepository
import com.batache.dokany.db.products.ProductsRepository
import com.batache.dokany.model.pojo.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.io.IOException

class ProductDetailViewModel(
  private val productId: String,
  private val productsRepository: ProductsRepository,
  private val productSellersRepository: ProductSellersRepository,
  private val favoritesRepository: FavoritesRepository,
  private val cartProductsRepository: CartProductsRepository
) : ViewModel() {

  private val firestore = Firebase.firestore
  private val userId: String get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
  private val favoriteDocumentId get() = "${userId}_$productId"

  val productDetails: LiveData<Product> get() = productsRepository.getProduct(productId)

  val productSellers: LiveData<List<ProductSellerDetails>>
    get() = productSellersRepository.getProductSellers(
      productId
    )

  val cartEvent: MediatorLiveData<CartEvent> = MediatorLiveData()
  var quantityInCart: Int = 0

  init {
    refreshDataFromRepository()
    syncWithCart()
  }

//  suspend fun getRecommendedSellerProductDetails(): LiveData<SellerProductDetailz> {
//    return productsRepository.getRecommendedSellerProductDetails(productId)
//  }

  private fun refreshDataFromRepository() {
    viewModelScope.launch {
      try {
        productsRepository.refreshProduct(productId)
        productSellersRepository.refreshSellersForProduct(productId)
//        _eventNetworkError.value = false
//        _isNetworkErrorShown.value = false

      } catch (networkError: IOException) {
        // Show a Toast error message and hide the progress bar.
//        if (playlist.value.isNullOrEmpty())
//          _eventNetworkError.value = true
      }
    }
  }

  fun addToFavorites() {
    var favorite = Favorite(
      productId = productId
    )

    viewModelScope.launch {
      favoritesRepository.insert(favorite)

      if (userId.isNotEmpty()) {
        val document = firestore.collection("favorites").document(favoriteDocumentId)
        favorite = favorite.copy(
          userId = userId
        )
        document.set(favorite)
      }
    }
  }

  fun isInFavorites(): LiveData<Boolean> {
    val liveData: MediatorLiveData<Boolean> = MediatorLiveData()

    viewModelScope.launch {
      liveData.postValue(favoritesRepository.isFavorited(productId))
    }

    return liveData
  }

  fun removeFromFavorites() {
    viewModelScope.launch {
      favoritesRepository.delete(productId)

      if (userId.isNotEmpty()) {
        firestore.collection("favorites").document(favoriteDocumentId).delete()
      }
    }
  }

  fun syncWithCart() {
    viewModelScope.launch {
      val quantity = cartProductsRepository.getQuantity(productId)
      quantity?.let {
        quantityInCart = quantity
        cartEvent.postValue(CartEvent.SyncComplete(quantity > 0))
      }
    }
  }

  fun addToCart(sellerId: String?, quantity: Int) {
    sellerId?.let {
      viewModelScope.launch {
        cartProductsRepository.insert(
          CartProduct(
            productId = productId,
            sellerId = sellerId,
            quantity = quantity
          )
        )
        quantityInCart = quantity
        cartEvent.postValue(CartEvent.AddedToCart)
      }
    }
  }

  fun updateCartQuantity(quantity: Int) {
    viewModelScope.launch {
      cartProductsRepository.updateQuantity(productId, quantity)
      quantityInCart = quantity
      cartEvent.postValue(CartEvent.QuantityChanged)
    }
  }

  fun removeFromCart() {
    viewModelScope.launch {
      cartProductsRepository.delete(productId)
      quantityInCart = 0
      cartEvent.postValue(CartEvent.RemovedFromCart)
    }
  }

  sealed class CartEvent {
    data class SyncComplete(val isInCart: Boolean) : CartEvent()
    object AddedToCart : CartEvent()
    object RemovedFromCart : CartEvent()
    object QuantityModified : CartEvent()
    object QuantityChanged : CartEvent()
  }

  companion object {
    const val TAG = "ProductDetailViewModel"
  }
}

class ProductDetailViewModelFactory(
  private val productId: String,
  private val productsRepository: ProductsRepository,
  private val productSellersRepository: ProductSellersRepository,
  private val favoritesRepository: FavoritesRepository,
  private val cartProductsRepository: CartProductsRepository
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return ProductDetailViewModel(
        productId,
        productsRepository,
        productSellersRepository,
        favoritesRepository,
        cartProductsRepository
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}