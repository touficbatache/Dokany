package com.batache.dokany.model.pojo.cart

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.batache.dokany.model.pojo.product.ProductResponse
import com.batache.dokany.model.pojo.Seller

@Keep
@Entity(
  tableName = "cart_products_table",
  foreignKeys = [
    ForeignKey(
      entity = ProductResponse::class,
      parentColumns = ["id"],
      childColumns = ["productId"]
    ),
    ForeignKey(
      entity = Seller::class,
      parentColumns = ["id"],
      childColumns = ["sellerId"]
    )
  ]
)
data class CartProductJoin(
  @PrimaryKey val productId: String = "",
  val sellerId: String = "",
  val quantity: Int? = null
)