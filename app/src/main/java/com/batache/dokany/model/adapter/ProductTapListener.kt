package com.batache.dokany.model.adapter

interface ProductTapListener {
  fun onSingleClick(productId: String)
  fun onDoubleClick(productId: String)
  fun onAddToCartClick(productId: String, productSellerIds: List<String>)
}