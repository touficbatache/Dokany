package com.batache.dokany.db.sellers

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.batache.dokany.model.pojo.Seller
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SellersRepository(private val sellersDao: SellersDao) {

  suspend fun getLocalSellersCount(): Int {
    return sellersDao.getSellersCount()
  }

  suspend fun refreshSellers() {
    withContext(Dispatchers.IO) {
      val snapshot = Firebase.firestore.collection("sellers").get().await()
      val products = snapshot.toObjects<Seller>()
      insertAll(products)
    }
  }

  val sellersLive: LiveData<List<Seller>> = sellersDao.getSellersLive().asLiveData()

  suspend fun getSellers(): List<Seller> = sellersDao.getSellers()

  suspend fun refreshSeller(id: String) {
    withContext(Dispatchers.IO) {
      val snapshot = Firebase.firestore.collection("sellers").document(id).get().await()
      val product = snapshot.toObject<Seller>()
      product?.let {
        insert(product)
      }
    }
  }

  fun getSeller(id: String): LiveData<Seller> = sellersDao.getSeller(id).asLiveData()

  @WorkerThread
  suspend fun insert(seller: Seller) {
    sellersDao.insert(seller)
  }

  @WorkerThread
  suspend fun insertAll(sellers: List<Seller>) {
    sellersDao.insertAll(sellers)
  }

  @WorkerThread
  suspend fun delete(id: String) {
    sellersDao.delete(id)
  }
}