package com.batache.dokany.db.product_sellers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.batache.dokany.model.pojo.Product
import com.batache.dokany.model.pojo.ProductSellerDetails
import com.batache.dokany.model.pojo.ProductSeller
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductSellersDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(productSeller: ProductSeller)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(productSellers: List<ProductSeller>)

  @Query("SELECT ps.sellerId as id, s.name as name, s.rating as starsRating, ps.price as price, ps.quantity as quantity FROM sellers_table s JOIN product_sellers_table ps ON s.id = ps.sellerId WHERE ps.productId = :productId")
  fun getProductSellers(productId: String): Flow<List<ProductSellerDetails>>

  @Query("DELETE FROM product_sellers_table WHERE sellerId = :sellerId")
  suspend fun delete(sellerId: String)
}