package com.batache.dokany.db.products

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.batache.dokany.model.pojo.Product
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductsRepository(private val productsDao: ProductsDao) {

  // Products //

  @WorkerThread
  suspend fun insert(product: Product) {
    productsDao.insert(product)
  }

  @WorkerThread
  suspend fun insertAll(products: List<Product>) {
    productsDao.insertAll(products)
  }

  suspend fun refreshProducts() {
    withContext(Dispatchers.IO) {
      val snapshot = Firebase.firestore.collection("products").get().await()
      val products = snapshot.toObjects<Product>()
      insertAll(products)
    }
  }

  val products: LiveData<List<Product>> = productsDao.getProducts().asLiveData()

  suspend fun refreshProduct(id: String) {
    withContext(Dispatchers.IO) {
      val snapshot = Firebase.firestore.collection("products").document(id).get().await()
      val product = snapshot.toObject<Product>()
      product?.let {
        insert(product)
      }
    }
  }

  fun getProduct(id: String): LiveData<Product> = productsDao.getProduct(id).asLiveData()

//  // Product sellers (join) //
//
//  suspend fun getSellersForProduct(id: String): List<SellerProductDetailz> {
//    val snapshot = Firebase.firestore.collection("sellerProducts")
//      .whereEqualTo("productId", id)
//      .get()
//      .await()
//    return snapshot.toObjects<SellerProductDetailz>()
//  }

//  suspend fun getRecommendedSellerProductDetails(productId: String): LiveData<SellerProductDetailz> {
//    val liveData: MediatorLiveData<SellerProductDetailz> = MediatorLiveData()
//
//    withContext(Dispatchers.IO) {
//      val recommendedSellerSnapshot =
//        Firebase.firestore.collection("recommendedSellers")
//          .whereEqualTo("productId", productId)
//          .get()
//          .await()
//      if (recommendedSellerSnapshot.documents.isNotEmpty()) {
//        val recommendedSeller = recommendedSellerSnapshot.documents[0].toObject<RecommendedSeller>()
//        val sellerProductDetailsSnapshot =
//          Firebase.firestore.collection("sellerProducts")
//            .whereEqualTo("sellerId", recommendedSeller?.sellerId)
//            .whereEqualTo("productId", productId)
//            .get()
//            .await()
//        if (sellerProductDetailsSnapshot.documents.isNotEmpty()) {
//          liveData.postValue(sellerProductDetailsSnapshot.documents[0].toObject<SellerProductDetailz>())
//        }
//      }
//    }
//
//    return liveData
//  }

  @WorkerThread
  suspend fun delete(id: String) {
    productsDao.delete(id)
  }
}