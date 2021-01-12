package com.batache.dokany.db.cart_products

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.batache.dokany.model.pojo.CartProduct
import com.batache.dokany.model.pojo.CartProductDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CartProductsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(cartProduct: CartProduct)

  @Query("SELECT cp.productId as productId, p.title as productTitle, p.relevantDetail as productRelevantDetail, p.images as productImages, ps.price as productPrice, cp.sellerId as sellerId, s.name as sellerName, cp.quantity as quantity FROM products_table p JOIN cart_products_table cp ON p.id = cp.productId JOIN sellers_table s ON s.id = cp.sellerId JOIN product_sellers_table ps ON ps.sellerId = cp.sellerId WHERE s.id = cp.sellerId")
  fun getCartProducts(): Flow<List<CartProductDetails>>

  @Query("SELECT quantity FROM cart_products_table WHERE productId = :productId")
  suspend fun getQuantity(productId: String): Int?

  @Query("UPDATE cart_products_table SET quantity = :quantity WHERE productId = :productId")
  suspend fun updateQuantity(productId: String, quantity: Int)

  @Query("DELETE FROM cart_products_table WHERE productId = :productId")
  suspend fun delete(productId: String)
}