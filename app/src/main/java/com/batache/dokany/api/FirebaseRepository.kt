package com.batache.dokany.api

import android.net.Uri
import com.batache.dokany.model.pojo.*
import com.batache.dokany.model.pojo.cart.CartProductJoin
import com.batache.dokany.model.pojo.product.ProductResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRepository {

  private val currentUser get() = FirebaseAuth.getInstance().currentUser
  val currentUserId get() = currentUser?.uid

  val isUserSignedIn get() = currentUser != null

//  val api by lazy { ApiClient.getApiInterface() }

  /**
   * Firestore User Operations
   */
  suspend fun signInWithCredential(credential: PhoneAuthCredential): CompletionResponse {
    return try {
      val snapshot = FirebaseAuth
        .getInstance()
        .signInWithCredential(credential)
        .awaitCompletion()

      CompletionResponse(isSuccessful = snapshot.isSuccessful, exception = snapshot.exception)
    } catch (e: Exception) {
      CompletionResponse(isSuccessful = false, exception = e)
    }
  }

  suspend fun sendVerificationEmail(
    email: String,
    actionCodeSettings: ActionCodeSettings
  ): CompletionResponse {
    return try {
      val snapshot = FirebaseAuth
        .getInstance()
        .sendSignInLinkToEmail(email, actionCodeSettings)
        .awaitCompletion()

      CompletionResponse(isSuccessful = snapshot.isSuccessful, exception = snapshot.exception)
    } catch (e: Exception) {
      CompletionResponse(isSuccessful = false, exception = e)
    }
  }

  fun createCurrentUserInFirestore(onCompleteListener: OnCompleteListener<Void>) {
    currentUser?.let {
      Firebase.firestore
        .collection(FIRESTORE_COLLECTION_USERS)
        .document(it.uid)
        .set(
          User(
            fullName = it.displayName ?: "",
            addresses = ArrayList()
          )
        )
        .addOnCompleteListener(onCompleteListener)
    }
  }

  suspend fun updateUser(
    updatedDisplayName: String? = currentUser?.displayName,
//    updatedPhotoUri: Uri? = currentUser?.photoUrl
  ): Boolean {
    currentUser?.let {
      val profileUpdates = userProfileChangeRequest {
        displayName = updatedDisplayName
//        photoUri = updatedPhotoUri
      }

      return it.updateProfile(profileUpdates).awaitCompletion().isSuccessful
    }

    return false
  }

  suspend fun isCurrentUserInFirestore(): Boolean? {
    currentUser?.let {
      val snapshot = Firebase.firestore
        .collection(FIRESTORE_COLLECTION_USERS)
        .document(it.uid)
        .get()
        .await()

      return snapshot.exists()
    }

    return null
  }

  suspend fun getCurrentUserAddresses(): List<Address> {
    var userAddresses: List<Address> = ArrayList()

    currentUser?.let {
      val snapshot = Firebase.firestore
        .collection(FIRESTORE_COLLECTION_USERS)
        .document(it.uid)
        .get()
        .await()

      val user = snapshot.toObject<User>()

      user?.addresses?.let { addresses ->
        userAddresses = addresses
      }
    }

    return userAddresses
  }

  fun uploadBuildingImage(
    imageUri: Uri,
    onProgressListener: OnProgressListener<UploadTask.TaskSnapshot>,
    onCompleteListener: (Task<UploadTask.TaskSnapshot>, String) -> Unit
  ) {
    currentUser?.let {
      val imageId = UUID.randomUUID()
      val imageTitle = "${it.uid}_$imageId"

      FirebaseStorage.getInstance().reference
        .child("$STORAGE_PATH_BUILDING_IMAGES/$imageTitle")
        .putFile(imageUri)
        .addOnProgressListener(onProgressListener)
        .addOnCompleteListener { completeTask ->
          onCompleteListener.invoke(completeTask, imageTitle)
        }
    }
  }

  fun addAddressInCurrentUser(address: Address, onCompleteListener: OnCompleteListener<Void>) {
    currentUser?.let {
      Firebase.firestore
        .collection(FIRESTORE_COLLECTION_USERS)
        .document(it.uid)
        .update(FIRESTORE_FIELD_ADDRESSES, FieldValue.arrayUnion(address))
        .addOnCompleteListener(onCompleteListener)
    }
  }

  /**
   * Functions Product Operations
   */
  suspend fun getProducts(): ProductRepoResponse {
    return try {
      val products = ApiClient.getApiInterface().getProducts().body() ?: ArrayList()

      ProductRepoResponse(
        isSuccessful = true,
        data = products
      )
    } catch (e: Exception) {
      ProductRepoResponse(isSuccessful = false, error = e.message)
    }
  }

  data class ProductRepoResponse(
    val isSuccessful: Boolean,
    val error: String? = null,
    val data: List<ProductResponse>? = null
  )

  /**
   * Firestore Favorite Operations
   */
  fun addToFavorites(productId: String) {
    currentUser?.let {
      Firebase.firestore
        .collection(FIRESTORE_COLLECTION_FAVORITES)
        .document(getFavoriteDocumentId(it.uid, productId))
        .set(
          FirestoreFavorite(
            productId = productId,
            userId = it.uid
          )
        )
    }
  }

  fun removeToFavorites(productId: String) {
    currentUser?.let {
      Firebase.firestore
        .collection(FIRESTORE_COLLECTION_FAVORITES)
        .document(getFavoriteDocumentId(it.uid, productId))
        .delete()
    }
  }

  private fun getFavoriteDocumentId(userId: String, productId: String): String {
    return "${userId}_${productId}"
  }

  data class FirestoreFavorite(
    val productId: String,
    val userId: String
  )

  /**
   * Firestore Seller Operations
   */
  suspend fun getSellers(): List<Seller> {
    return ApiClient.getApiInterface().getSellers().body() ?: ArrayList()
  }

  suspend fun getSeller(id: String): Seller? {
    return ApiClient.getApiInterface().getSeller(id).body()
  }

  /**
   * Functions Order Operations
   */
  suspend fun getOrders(): List<Order> {
    return try {
      ApiClient.getApiInterface().getOrders().body() ?: ArrayList()
    } catch (exception: Exception) {
      ArrayList()
    }
  }

  suspend fun getOrder(id: String): Order? {
    return try {
      ApiClient.getApiInterface().getOrder(id).body()
    } catch (exception: Exception) {
      null
    }
  }

  suspend fun placeOrder(order: FirestoreOrder): Boolean {
    return ApiClient.getApiInterface().placeOrder(order).isSuccessful
  }

  data class FirestoreOrder(
    val products: List<CartProductJoin>,
    val deliveryAddress: String,
    val deliveryTime: String,
    val additionalInfo: String,
    val allowSubstitutions: Boolean
  )

  /**
   * Functions Family Bond Operations
   */
  suspend fun getFamilyBond(): FamilyBondRepoResponse {
    try {
      val familyBondResponse = ApiClient.getApiInterface().getFamilyBond()

      if (familyBondResponse.code() == 400) {
        return FamilyBondRepoResponse(isSuccessful = true, error = ERROR_NO_FAMILY_BOND)
      }

      if (familyBondResponse.isSuccessful) {
        return FamilyBondRepoResponse(
          isSuccessful = true,
          data = familyBondResponse.body()
        )
      } else {
        throw Exception(familyBondResponse.code().toString())
      }
    } catch (e: Exception) {
      return FamilyBondRepoResponse(isSuccessful = false, error = e.message)
    }
  }

  data class FamilyBondRepoResponse(
    val isSuccessful: Boolean,
    val error: String? = null,
    val data: FamilyBond? = null
  )

  data class CompletionResponse(
    val isSuccessful: Boolean,
    val exception: Exception? = null
  )

  /**
   * Extensions
   */
  private fun <T> Any.deserializeAs(classOfT: Class<T>): T? {
    return Gson().fromJson(Gson().toJson(this), classOfT)
  }

  private fun <T> Any.deserializeAs(typeOfT: Type): T? {
    return Gson().fromJson(Gson().toJson(this), typeOfT)
  }

  /**
   * Awaits for completion of the task without blocking a thread.
   *
   * This suspending function is cancellable.
   * If the [Job] of the current coroutine is cancelled or completed while this suspending function is waiting, this function
   * stops waiting for the completion stage and immediately resumes with [CancellationException].
   */
  private suspend fun <T> Task<T>.awaitCompletion(): Task<T> {
    // fast path
    if (isComplete) {
      val e = exception
      return if (e == null) {
        if (isCanceled) {
          throw CancellationException("Task $this was cancelled normally.")
        } else {
          @Suppress("UNCHECKED_CAST")
          this
        }
      } else {
        throw e
      }
    }

    return suspendCancellableCoroutine { cont ->
      addOnCompleteListener {
        val e = exception
        if (e == null) {
          @Suppress("UNCHECKED_CAST")
          if (isCanceled) cont.cancel() else cont.resume(this)
        } else {
          cont.resumeWithException(e)
        }
      }
    }
  }

  companion object {
    /**
     * Firestore Collections
     */
    private const val FIRESTORE_COLLECTION_USERS = "users"
    private const val FIRESTORE_COLLECTION_FAVORITES = "favorites"

    /**
     * Firestore Fields
     */
    private const val FIRESTORE_FIELD_ADDRESSES = "addresses"

    /**
     * Storage Paths
     */
    private const val STORAGE_PATH_BUILDING_IMAGES = "buildingImages"

    /**
     * Error messages
     */
    const val ERROR_NO_FAMILY_BOND = "ERROR_NO_FAMILY_BOND"
  }
}