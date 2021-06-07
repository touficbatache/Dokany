package com.batache.dokany.db.products

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.batache.dokany.db.base.BaseRepository
import com.batache.dokany.model.pojo.product.Product
import com.batache.dokany.model.pojo.product.ProductResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

class ProductsRepository(private val productsDao: ProductsDao) : BaseRepository() {

  @WorkerThread
  suspend fun insert(product: ProductResponse) {
    productsDao.insert(product)
  }

  @WorkerThread
  suspend fun insertAll(products: List<ProductResponse>) {
    productsDao.insertAll(products)
  }

  suspend fun refreshProducts() {
    val productsRepoResponse = firebaseRepo.getProducts()
    if (productsRepoResponse.isSuccessful) {
      productsRepoResponse.data?.let { insertAll(it) }
    }
  }

  val products = productsDao.getProducts().asLiveData()

  // TODO: Get product from Firebase, not from Room
  fun getProduct(id: String): LiveData<Product> = productsDao.getProduct(id).asLiveData()

  val productsInFavorites: LiveData<List<Product>> =
    productsDao.getProductsInFavorites().asLiveData()

  @WorkerThread
  suspend fun addToFavorites(productId: String): Boolean {
    if (FirebaseAuth.getInstance().currentUser == null) {
      return false
    }
    productsDao.addToFavorites(productId)
    firebaseRepo.addToFavorites(productId)
    return true
  }

  @WorkerThread
  suspend fun isInFavorites(productId: String): Boolean {
    return productsDao.isInFavorites(productId)
  }

  fun isInFavoritesLive(productId: String): Flow<Product?> {
    return productsDao.isInFavoritesLive(productId)
  }

  @WorkerThread
  suspend fun removeFromFavorites(productId: String) {
    productsDao.removeFromFavorites(productId)
    firebaseRepo.removeToFavorites(productId)
  }

  @WorkerThread
  suspend fun delete(id: String) {
    productsDao.delete(id)
  }
}