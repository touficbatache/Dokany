package com.batache.dokany.db.favorites

import androidx.annotation.WorkerThread
import com.batache.dokany.db.favorites.FavoritesDao
import com.batache.dokany.model.pojo.Favorite
import com.batache.dokany.model.pojo.Product
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val favoritesDao: FavoritesDao) {

  val allFavoritedProducts: Flow<List<Product>> = favoritesDao.getFavoritedProducts()

  @WorkerThread
  suspend fun insert(favorite: Favorite) {
    favoritesDao.insert(favorite)
  }

  @WorkerThread
  suspend fun isFavorited(productId: String): Boolean {
    return favoritesDao.isFavorited(productId)
  }

  @WorkerThread
  suspend fun delete(productId: String) {
    favoritesDao.delete(productId)
  }
}