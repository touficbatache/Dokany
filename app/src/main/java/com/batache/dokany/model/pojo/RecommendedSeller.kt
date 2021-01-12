package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import androidx.room.PrimaryKey

@Keep
data class RecommendedSeller(
  @PrimaryKey val id: String = "",
  val productId: String = "",
  val sellerId: String = ""
)