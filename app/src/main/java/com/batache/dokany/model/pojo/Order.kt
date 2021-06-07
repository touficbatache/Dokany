package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Order(
  val id: String = "",
  val date: Long? = null,
  val status: OrderStatus? = null,
  val products: List<OrderProduct> = ArrayList(),
  val deliveryAddress: String = "",
  val deliveryTime: String = "",
  val deliveryPrice: Int? = null,
  val additionalInfo: String = "",
  val allowSubstitutions: Boolean? = null
)

data class OrderProduct(
  val productId: String = "",
  val title: String = "",
  val relevantDetail: String = "",
  val images: List<String> = ArrayList(),
  val sellerId: String = "",
//  val sellerName: String = "",
  val unitPrice: Int? = null,
  val quantity: Int? = null
)

enum class OrderStatus {
  @SerializedName("processing")
  PROCESSING,

  @SerializedName("delivered")
  DELIVERED
}