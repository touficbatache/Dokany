package com.batache.dokany.db.cart_products

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.batache.dokany.model.pojo.cart.CartProductJoin
import com.batache.dokany.model.pojo.cart.CartProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface CartProductsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(cartProduct: CartProductJoin)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(cartProducts: List<CartProductJoin>)

  @Query("SELECT cp.productId as productId, p.title as productTitle, p.relevantDetail as productRelevantDetail, p.images as productImages, p.sellers as productSellers, cp.sellerId as sellerId, cp.quantity as quantity FROM products_table p JOIN cart_products_table cp ON p.id = cp.productId")
  fun getCartProductsLive(): Flow<List<CartProduct>>

  @Query("SELECT cp.productId as productId, p.title as productTitle, p.relevantDetail as productRelevantDetail, p.images as productImages, p.sellers as productSellers, cp.sellerId as sellerId, cp.quantity as quantity FROM products_table p JOIN cart_products_table cp ON p.id = cp.productId")
  suspend fun getCartProducts(): List<CartProduct>

  @Query("SELECT * FROM cart_products_table")
  suspend fun getCartProductsJoin(): List<CartProductJoin>

//  @Query("SELECT * FROM cart_products_table")
//  fun getCartProducts(): Flow<List<CartProduct>>

  @Query("SELECT quantity FROM cart_products_table WHERE productId = :productId")
  suspend fun getQuantity(productId: String): Int?

  @Query("SELECT sellerId FROM cart_products_table WHERE productId = :productId")
  suspend fun getSellerId(productId: String): String?

  @Query("UPDATE cart_products_table SET quantity = :quantity WHERE productId = :productId")
  suspend fun updateQuantity(productId: String, quantity: Int)

  @Query("DELETE FROM cart_products_table WHERE productId = :productId")
  suspend fun delete(productId: String)

  @Query("DELETE FROM cart_products_table")
  suspend fun deleteAll()
}