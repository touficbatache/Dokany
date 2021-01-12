package com.batache.dokany.db.sellers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.batache.dokany.model.pojo.Seller
import kotlinx.coroutines.flow.Flow

@Dao
interface SellersDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(product: Seller)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<Seller>)

  @Query("SELECT * FROM sellers_table")
  fun getSellersLive(): Flow<List<Seller>>

  @Query("SELECT * FROM sellers_table")
  suspend fun getSellers(): List<Seller>

  @Query("SELECT count(*) FROM sellers_table")
  suspend fun getSellersCount(): Int

  @Query("SELECT * FROM sellers_table WHERE id = :id")
  fun getSeller(id: String): Flow<Seller>

  @Query("SELECT EXISTS (SELECT 1 FROM sellers_table WHERE id = :id)")
  suspend fun exists(id: String): Boolean

  @Query("DELETE FROM sellers_table WHERE id = :id")
  suspend fun delete(id: String)

  @Query("DELETE FROM sellers_table")
  suspend fun deleteAll()
}