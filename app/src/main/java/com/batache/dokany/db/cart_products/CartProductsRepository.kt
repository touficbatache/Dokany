package com.batache.dokany.db.cart_products

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.batache.dokany.model.pojo.CartProduct
import com.batache.dokany.model.pojo.CartProductDetails

class CartProductsRepository(private val cartProductsDao: CartProductsDao) {

  val cartProductsDetails: LiveData<List<CartProductDetails>> =
    cartProductsDao.getCartProducts().asLiveData()

  @WorkerThread
  suspend fun insert(cartProduct: CartProduct) {
    cartProductsDao.insert(cartProduct)
  }

  @WorkerThread
  suspend fun getQuantity(productId: String): Int? {
    return cartProductsDao.getQuantity(productId)
  }

  @WorkerThread
  suspend fun updateQuantity(productId: String, quantity: Int) {
    cartProductsDao.updateQuantity(productId, quantity)
  }

  @WorkerThread
  suspend fun delete(productId: String) {
    cartProductsDao.delete(productId)
  }
}