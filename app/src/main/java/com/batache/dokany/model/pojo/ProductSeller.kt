package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Keep
@Entity(
  tableName = "product_sellers_table",
  primaryKeys = ["productId", "sellerId"],
  foreignKeys = [
    ForeignKey(
      entity = Product::class,
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
data class ProductSeller(
  @DocumentId val firestoreDocumentId: String = "",
  val productId: String = "",
  val sellerId: String = "",
  val price: Int? = null,
  val quantity: Int? = null
)