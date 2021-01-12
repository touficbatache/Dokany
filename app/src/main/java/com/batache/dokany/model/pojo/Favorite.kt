package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Keep
@Entity(
  tableName = "favorites_table",
  foreignKeys = [
    ForeignKey(
      entity = Product::class,
      parentColumns = ["id"],
      childColumns = ["productId"]
    )
  ]
)
data class Favorite(
  @DocumentId val firestoreDocumentId: String = "",
  @PrimaryKey val productId: String = "",
  val userId: String = ""
)