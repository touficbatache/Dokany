package com.batache.dokany.model.pojo

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId

@Keep
data class User(
  @DocumentId val id: String = "",
  val fullName: String = ""
)