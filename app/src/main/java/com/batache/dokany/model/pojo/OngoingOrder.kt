package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OngoingOrder(
  val orderId: String = "",
  val latitude: String = "",
  val longitude: String = ""
)