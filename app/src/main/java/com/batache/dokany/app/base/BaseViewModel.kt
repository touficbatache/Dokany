package com.batache.dokany.app.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.batache.dokany.api.FirebaseRepository

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

  /**
   * Firebase repository
   */
  protected val firebaseRepo by lazy { FirebaseRepository() }

  val isUserSignedIn get() = firebaseRepo.isUserSignedIn

  /**
   * Events
   */
  protected var _eventNetworkError = MutableLiveData(false)
  val eventNetworkError: LiveData<Boolean>
    get() = _eventNetworkError

  protected var _isNetworkErrorShown = MutableLiveData(false)
  val isNetworkErrorShown: LiveData<Boolean>
    get() = _isNetworkErrorShown

  protected var _eventAuthError = MutableLiveData(false)
  val eventAuthError: LiveData<Boolean>
    get() = _eventAuthError

  protected var _eventFirestoreError = MutableLiveData(false)
  val eventFirestoreError: LiveData<Boolean>
    get() = _eventFirestoreError

  protected var _isFirestoreErrorShown = MutableLiveData(false)
  val isFirestoreErrorShown: LiveData<Boolean>
    get() = _isFirestoreErrorShown

  protected var _eventFirestoreSuccess = MutableLiveData(false)
  val eventFirestoreSuccess: LiveData<Boolean>
    get() = _eventFirestoreSuccess

  protected var _eventFirebaseStorage: MutableLiveData<FirebaseStorageEvent> = MutableLiveData()
  val eventFirebaseStorage: LiveData<FirebaseStorageEvent> get() = _eventFirebaseStorage
}

sealed class FirebaseStorageEvent {
  class Progress(val progress: Int) : FirebaseStorageEvent()
  class Success(val imageTitle: String) : FirebaseStorageEvent()
  object Failure : FirebaseStorageEvent()
}