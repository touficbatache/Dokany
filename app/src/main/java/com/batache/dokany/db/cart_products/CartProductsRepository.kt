package com.batache.dokany.db.cart_products

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.batache.dokany.model.pojo.cart.CartProduct
import com.batache.dokany.model.pojo.cart.CartProductJoin

class CartProductsRepository(private val cartProductsDao: CartProductsDao) {

  val cartProductsLive: LiveData<List<CartProduct>> = cartProductsDao.getCartProductsLive().asLiveData()

  suspend fun getCartProducts() = cartProductsDao.getCartProducts()

  suspend fun getCartProductsJoin() = cartProductsDao.getCartProductsJoin()

  @WorkerThread
  suspend fun insert(cartProduct: CartProductJoin) {
    cartProductsDao.insert(cartProduct)
  }

  @WorkerThread
  suspend fun insertAll(cartProducts: List<CartProductJoin>) {
    cartProductsDao.insertAll(cartProducts)
  }

  @WorkerThread
  suspend fun getQuantity(productId: String): Int? {
    return cartProductsDao.getQuantity(productId)
  }

  @WorkerThread
  suspend fun getSellerId(productId: String): String? {
    return cartProductsDao.getSellerId(productId)
  }

  @WorkerThread
  suspend fun updateQuantity(productId: String, quantity: Int) {
    cartProductsDao.updateQuantity(productId, quantity)
  }

  @WorkerThread
  suspend fun delete(productId: String) {
    cartProductsDao.delete(productId)
  }

  @WorkerThread
  suspend fun deleteAll() {
    cartProductsDao.deleteAll()
  }
}