package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Keep
@Entity(tableName = "sellers_table")
data class Seller(
  @PrimaryKey @DocumentId val id: String = "",
  val name: String = "",
  val profilePicture: String = "",
  val locationLatitude: String = "",
  val locationLongitude: String = "",
  var rating: Double? = null,
  val productCount: Int? = null,
  val images: List<String> = ArrayList()
)