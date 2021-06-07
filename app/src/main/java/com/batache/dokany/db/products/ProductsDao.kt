package com.batache.dokany.db.products

import androidx.room.*
import com.batache.dokany.model.pojo.product.ProductResponse
import com.batache.dokany.model.pojo.product.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(product: ProductResponse)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<ProductResponse>)

  @Query("SELECT *, EXISTS (SELECT 1 FROM cart_products_table WHERE productId = p.id) as isInCart FROM products_table p")
  fun getProducts(): Flow<List<Product>>

  @Query("SELECT *, EXISTS (SELECT 1 FROM cart_products_table WHERE productId = p.id) as isInCart FROM products_table p WHERE id = :id")
  fun getProduct(id: String): Flow<Product>

  @Query("SELECT count(*) FROM products_table")
  suspend fun getProductCount(): Int

  @Query("SELECT EXISTS (SELECT 1 FROM products_table WHERE id = :id)")
  suspend fun exists(id: String): Boolean

  @Query("UPDATE products_table SET isInFavorites = 1 WHERE id = :productId")
  suspend fun addToFavorites(productId: String)

  @Query("SELECT *, EXISTS (SELECT 1 FROM cart_products_table WHERE productId = p.id) as isInCart FROM products_table p WHERE isInFavorites = 1")
  fun getProductsInFavorites(): Flow<List<Product>>

  @Query("SELECT EXISTS(SELECT * FROM products_table WHERE id = :productId AND isInFavorites = 1)")
  suspend fun isInFavorites(productId: String): Boolean

  @Query("SELECT *, EXISTS (SELECT 1 FROM cart_products_table WHERE productId = p.id) as isInCart FROM products_table p WHERE id = :productId AND isInFavorites = 1")
  fun isInFavoritesLive(productId: String): Flow<Product?>

  @Query("UPDATE products_table SET isInFavorites = 0 WHERE id = :productId")
  suspend fun removeFromFavorites(productId: String)

  @Query("DELETE FROM products_table WHERE id = :id")
  suspend fun delete(id: String)

  @Query("DELETE FROM products_table")
  suspend fun deleteAll()
}