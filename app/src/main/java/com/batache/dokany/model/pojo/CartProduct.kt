package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Keep
@Entity(
  tableName = "cart_products_table",
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
data class CartProduct(
  @DocumentId val firestoreDocumentId: String = "",
  @PrimaryKey val productId: String = "",
  val sellerId: String = "",
  val quantity: Int? = null
)