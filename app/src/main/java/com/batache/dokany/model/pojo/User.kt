package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId

@Keep
data class User(
  @DocumentId val id: String = "",
  val fullName: String = "",
  val addresses: List<Address> = ArrayList()
)

data class Address(
  val title: String = "",
  val latitude: String = "",
  val longitude: String = "",
  val area: String = "",
  val street: String = "",
  val building: String = "",
  val floorApartment: String = "",
  val additionalInstructions: String? = null,
  val image: String? = null
)