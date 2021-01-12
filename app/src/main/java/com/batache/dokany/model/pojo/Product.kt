package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Keep
@Entity(tableName = "products_table")
data class Product(
  @PrimaryKey @DocumentId val id: String = "",
  val title: String = "",
  val description: String = "",
  var relevantDetail: String = "",
  val category: String = "",
  val images: List<String> = ArrayList(),
  val favoriteCount: Int? = null,
)