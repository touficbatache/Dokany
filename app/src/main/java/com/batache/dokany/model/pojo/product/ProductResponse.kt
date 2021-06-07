package com.batache.dokany.model.pojo.product

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "products_table")
data class ProductResponse(
  @PrimaryKey val id: String = "",
  val title: String = "",
  val description: String = "",
  val relevantDetail: String = "",
  val category: String = "",
  val images: List<String> = ArrayList(),
  val favoriteCount: Int? = null,
  val isInFavorites: Boolean = false,
  val sellers: List<ProductSeller> = ArrayList()
)

data class ProductSeller(
  val id: String = "",
  val name: String = "",
  val locationLatitude: String = "",
  val locationLongitude: String = "",
  val rating: Double? = null,
  val productCount: Int? = null,
  val images: List<String> = ArrayList(),
  val price: Int? = null,
  val quantity: Int? = null
)