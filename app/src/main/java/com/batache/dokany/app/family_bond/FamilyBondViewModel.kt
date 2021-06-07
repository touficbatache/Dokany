package com.batache.dokany.app.family_bond

import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.model.pojo.FamilyBond
import com.batache.dokany.model.pojo.FamilyBondMember
import kotlinx.coroutines.launch

class FamilyBondViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * Live data.
   */
  private var _eventFamilyBond: MutableLiveData<FamilyBondEvent> = MutableLiveData()
  val eventFamilyBond: LiveData<FamilyBondEvent>
    get() = _eventFamilyBond

  fun getFamilyBondIfAvailable() {
    viewModelScope.launch {
      val familyBondRepoResponse = firebaseRepo.getFamilyBond()

      if (familyBondRepoResponse.isSuccessful) {
        _eventFamilyBond.postValue(familyBondRepoResponse.data?.let { FamilyBondEvent.Success(it) })
      } else {
        _eventFamilyBond.postValue(familyBondRepoResponse.error?.let { FamilyBondEvent.Failure(it) })
      }
    }
  }

  fun isCurrentUserAdmin(admins: List<FamilyBondMember>): Boolean {
    return admins.any { it.userId == firebaseRepo.currentUserId }
  }

  fun filterOutCurrentUser(members: List<FamilyBondMember>): List<FamilyBondMember> {
    return members.toMutableList().filter { it.userId != firebaseRepo.currentUserId }
  }

  sealed class FamilyBondEvent {
    class Success(val familyBond: FamilyBond) : FamilyBondEvent()
    class Failure(val error: String) : FamilyBondEvent()
  }

  companion object {
    const val TAG = "FamilyBondViewModel"
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(FamilyBondViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return FamilyBondViewModel(app) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}

