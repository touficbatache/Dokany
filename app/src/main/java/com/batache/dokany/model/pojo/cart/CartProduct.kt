package com.batache.dokany.model.pojo.cart

import com.batache.dokany.model.pojo.product.ProductSeller

data class CartProduct(
  val productId: String = "",
  val productTitle: String = "",
  var productRelevantDetail: String = "",
  val productImages: List<String> = ArrayList(),
  val productSellers: List<ProductSeller>? = null,
  val sellerId: String = "",
  val quantity: Int? = null
)