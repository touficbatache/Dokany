package com.batache.dokany.model.pojo

data class CartProductDetails(
  val productId: String = "",
  val productTitle: String = "",
  var productRelevantDetail: String = "",
  val productImages: List<String> = ArrayList(),
  val productPrice: Int? = null,
  val sellerId: String = "",
  val sellerName: String = "",
  val quantity: Int? = null
)