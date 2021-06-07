package com.batache.dokany.api

//import android.net.Uri
//import com.batache.dokany.api.Constants.Companion.FIRESTORE_COLLECTION_USERS
//import com.batache.dokany.api.Constants.Companion.FIRESTORE_FIELD_ADDRESSES
//import com.batache.dokany.api.Constants.Companion.STORAGE_PATH_BUILDING_IMAGES
//import com.batache.dokany.app.base.BaseViewModel
//import com.batache.dokany.model.pojo.Address
//import com.batache.dokany.model.pojo.User
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.android.gms.tasks.Task
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.firestore.ktx.toObject
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.storage.FirebaseStorage
//import com.google.firebase.storage.OnProgressListener
//import com.google.firebase.storage.UploadTask
//import kotlinx.coroutines.tasks.await
//import java.util.*
//import kotlin.collections.ArrayList

//private val currentUser get() = FirebaseAuth.getInstance().currentUser
//
//fun BaseViewModel.createCurrentUser(onCompleteListener: OnCompleteListener<Void>) {
//  currentUser?.let {
//    Firebase.firestore
//      .collection("users")
//      .document(it.uid)
//      .set(
//        User(
//          fullName = it.displayName ?: "",
//          addresses = ArrayList()
//        )
//      )
//      .addOnCompleteListener(onCompleteListener)
//  }
//}
//
//suspend fun BaseViewModel.getCurrentUserAddresses(): List<Address> {
//  var userAddresses: List<Address> = ArrayList()
//
//  currentUser?.let {
//    val snapshot = Firebase.firestore
//      .collection(FIRESTORE_COLLECTION_USERS)
//      .document(it.uid)
//      .get()
//      .await()
//
//    val user = snapshot.toObject<User>()
//
//    user?.addresses?.let { addresses ->
//      userAddresses = addresses
//    }
//  }
//
//  return userAddresses
//}
//
//fun BaseViewModel.uploadBuildingImage(
//  imageUri: Uri,
//  onProgressListener: OnProgressListener<UploadTask.TaskSnapshot>,
//  onCompleteListener: (Task<UploadTask.TaskSnapshot>, String) -> Unit
//) {
//  currentUser?.let {
//    val imageId = UUID.randomUUID()
//    val imageTitle = "${it.uid}_$imageId"
//
//    FirebaseStorage.getInstance().reference
//      .child("$STORAGE_PATH_BUILDING_IMAGES/$imageTitle")
//      .putFile(imageUri)
//      .addOnProgressListener(onProgressListener)
//      .addOnCompleteListener { completeTask ->
//        onCompleteListener.invoke(completeTask, imageTitle)
//      }
//  }
//}
//
//fun BaseViewModel.addAddressInCurrentUser(address: Address, onCompleteListener: OnCompleteListener<Void>) {
//  currentUser?.let {
//    Firebase.firestore
//      .collection(FIRESTORE_COLLECTION_USERS)
//      .document(it.uid)
//      .update(FIRESTORE_FIELD_ADDRESSES, FieldValue.arrayUnion(address))
//      .addOnCompleteListener(onCompleteListener)
//  }
//}
//
//private class Constants {
//  companion object {
//    /**
//     * Firestore Collections
//     */
//    const val FIRESTORE_COLLECTION_USERS = "users"
//
//    /**
//     * Firestore Fields
//     */
//    const val FIRESTORE_FIELD_ADDRESSES = "addresses"
//
//    /**
//     * Storage Paths
//     */
//    const val STORAGE_PATH_BUILDING_IMAGES = "buildingImages"
//  }
//}