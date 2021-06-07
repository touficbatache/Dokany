package com.batache.dokany.model.pojo.product

data class Product(
  val id: String = "",
  val title: String = "",
  val description: String = "",
  val relevantDetail: String = "",
  val category: String = "",
  val images: List<String> = ArrayList(),
  val favoriteCount: Int? = null,
  val isInFavorites: Boolean = false,
  val sellers: List<ProductSeller> = ArrayList(),
  val isInCart: Boolean = false
)