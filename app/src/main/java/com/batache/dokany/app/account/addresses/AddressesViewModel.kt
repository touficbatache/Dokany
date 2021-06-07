package com.batache.dokany.app.account.addresses

import android.net.Uri
import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.app.base.FirebaseStorageEvent
import com.batache.dokany.model.pojo.Address
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.launch

class AddressesViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * Live data.
   */
  private var _userAddresses: MutableLiveData<List<Address>> = MutableLiveData()
  val userAddresses: LiveData<List<Address>>
    get() = _userAddresses

  fun addAddress(address: Address) {
    firebaseRepo.addAddressInCurrentUser(address) {
      _eventFirestoreError.value = !it.isSuccessful
      _eventFirestoreSuccess.value = it.isSuccessful
    }
  }

  fun uploadPhoto(uri: Uri) {
    firebaseRepo.uploadBuildingImage(
      uri,
      {
        _eventFirebaseStorage.postValue(
          FirebaseStorageEvent.Progress(
            (it.bytesTransferred.toDouble() / it.totalByteCount.toDouble() * 100).toInt()
          )
        )
      },
      { task: Task<UploadTask.TaskSnapshot>, imageTitle: String ->
        if (task.isSuccessful) {
          _eventFirebaseStorage.postValue(FirebaseStorageEvent.Success(imageTitle))
        } else {
          _eventFirebaseStorage.postValue(FirebaseStorageEvent.Failure)
        }
      }
    )
  }

  fun getCurrentUserAddresses() {
    viewModelScope.launch {
      val userAddresses = firebaseRepo.getCurrentUserAddresses()
      _userAddresses.postValue(userAddresses)
    }
  }

  companion object {
    const val TAG = "AddressesViewModel"
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(AddressesViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return AddressesViewModel(app) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}