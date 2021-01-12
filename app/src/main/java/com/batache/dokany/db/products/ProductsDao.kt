package com.batache.dokany.db.products

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.batache.dokany.model.pojo.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(product: Product)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<Product>)

  @Query("SELECT * FROM products_table")
  fun getProducts(): Flow<List<Product>>

  @Query("SELECT count(*) FROM products_table")
  suspend fun getProductsCount(): Int

  @Query("SELECT * FROM products_table WHERE id = :id")
  fun getProduct(id: String): Flow<Product>

  @Query("SELECT EXISTS (SELECT 1 FROM products_table WHERE id = :id)")
  suspend fun exists(id: String): Boolean

  @Query("DELETE FROM products_table WHERE id = :id")
  suspend fun delete(id: String)

  @Query("DELETE FROM products_table")
  suspend fun deleteAll()
}