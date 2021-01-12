package com.batache.dokany.db.favorites

import androidx.room.*
import com.batache.dokany.model.pojo.Favorite
import com.batache.dokany.model.pojo.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(favorite: Favorite)

  @Query("SELECT * FROM products_table INNER JOIN favorites_table ON products_table.id = favorites_table.productId")
  fun getFavoritedProducts(): Flow<List<Product>>

  @Query("SELECT EXISTS(SELECT * FROM favorites_table WHERE productId = :productId)")
  suspend fun isFavorited(productId: String): Boolean

  @Query("DELETE FROM favorites_table WHERE productId = :productId")
  suspend fun delete(productId: String)

  @Query("DELETE FROM favorites_table")
  suspend fun deleteAll()
}