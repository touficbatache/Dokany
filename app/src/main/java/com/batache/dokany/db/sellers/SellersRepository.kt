package com.batache.dokany.db.sellers

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.batache.dokany.db.base.BaseRepository
import com.batache.dokany.model.pojo.Seller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SellersRepository(private val sellersDao: SellersDao) : BaseRepository() {

  suspend fun getLocalSellersCount(): Int {
    return sellersDao.getSellersCount()
  }

  suspend fun refreshSellers() {
    val sellers = firebaseRepo.getSellers()
    insertAll(sellers)
  }

  val sellersLive: LiveData<List<Seller>> = sellersDao.getSellersLive().asLiveData()

  suspend fun getSellers(): List<Seller> = sellersDao.getSellers()

  suspend fun refreshSeller(id: String) {
    withContext(Dispatchers.IO) {
      val seller = firebaseRepo.getSeller(id)
      seller?.let {
        insert(seller)
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