package com.batache.dokany.model.pojo

import androidx.annotation.Keep

@Keep
data class FamilyBond(
  val id: String = "",
  val name: String = "",
  val admins: List<FamilyBondMember> = ArrayList(),
  val members: List<FamilyBondMember> = ArrayList(),
  val lists: List<FamilyBondList> = ArrayList()
)

data class FamilyBondMember(
  val id: String = "",
  val userId: String = "",
  val name: String = "",
  val permissions: FamilyBondPermissions = FamilyBondPermissions()
)

fun FamilyBondMember.isLinkedToAccount() = userId.isNotBlank()

data class FamilyBondPermissions(
  val canPlaceOrders: Boolean? = null,
  val canCreateLists: Boolean? = null,
  val canRemoveItems: Boolean? = null
)

data class FamilyBondList(
  val id: String = "",
  val title: String = "",
  val items: List<FamilyBondListProduct> = ArrayList(),
  val lastEdited: FamilyBondListLastEdited = FamilyBondListLastEdited()
)

data class FamilyBondListProduct(
  val id: String = "",
  val title: String = "",
  val image: String = "",
  val addedBy: String = ""
)

data class FamilyBondListLastEdited(
  val memberId: String = "",
  val date: Long? = null
)