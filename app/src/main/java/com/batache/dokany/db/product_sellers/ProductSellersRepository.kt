package com.batache.dokany.db.product_sellers

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.batache.dokany.model.pojo.ProductSeller
import com.batache.dokany.model.pojo.ProductSellerDetails
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductSellersRepository(private val productSellersDao: ProductSellersDao) {

  // Product sellers (join) //

  @WorkerThread
  suspend fun insert(productSeller: ProductSeller) {
    productSellersDao.insert(productSeller)
  }

  @WorkerThread
  suspend fun insertAll(productSellers: List<ProductSeller>) {
    productSellersDao.insertAll(productSellers)
  }

  fun getProductSellers(productId: String): LiveData<List<ProductSellerDetails>> {
    return productSellersDao.getProductSellers(productId).asLiveData()
  }

  suspend fun refreshSellersForProduct(productId: String) {
    withContext(Dispatchers.IO) {
      val snapshot = Firebase.firestore.collection("sellerProducts")
        .whereEqualTo("productId", productId)
        .get()
        .await()
      val productSellers = snapshot.toObjects<ProductSeller>()
      insertAll(productSellers)
    }
  }

  suspend fun refreshProductSellers() {
    withContext(Dispatchers.IO) {
      val snapshot = Firebase.firestore.collection("sellerProducts").get().await()
      val productSellers = snapshot.toObjects<ProductSeller>()
      insertAll(productSellers)
    }
  }

  @WorkerThread
  suspend fun delete(sellerId: String) {
    productSellersDao.delete(sellerId)
  }
}