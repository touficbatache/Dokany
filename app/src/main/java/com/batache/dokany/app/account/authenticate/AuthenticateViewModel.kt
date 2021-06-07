package com.batache.dokany.app.account.authenticate

import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.actionCodeSettings
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthenticateViewModel(val application: DokanyApplication) : BaseViewModel(application) {

  private var _authStatus: MutableLiveData<AuthenticationStatus> = MutableLiveData()
  val authStatus: LiveData<AuthenticationStatus> get() = _authStatus

  private var verificationId: String = ""

  fun sendVerificationOtp(
    countryCode: String,
    phoneNumber: String,
    activity: AuthenticateActivity
  ) {
    _authStatus.postValue(AuthenticationStatus.PhoneVerificationSending)

    val mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
      object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(
          verificationId: String,
          token: PhoneAuthProvider.ForceResendingToken
        ) {
          super.onCodeSent(verificationId, token)

          this@AuthenticateViewModel.verificationId = verificationId
          _authStatus.postValue(AuthenticationStatus.PhoneVerificationSent(true))
        }

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
          signInWithPhoneAuthCredential(phoneAuthCredential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
          _authStatus.postValue(AuthenticationStatus.PhoneVerificationSent(false))
        }
      }

    val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance()).apply {
      setPhoneNumber("$countryCode$phoneNumber") // Phone number to verify
      setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
      setActivity(activity) // Activity (for callback binding)
      setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
    }.build()
    PhoneAuthProvider.verifyPhoneNumber(options)
  }

  fun signInWithOtp(otp: String) {
    if (verificationId.isEmpty()) {
      return
    }

    val credential = PhoneAuthProvider.getCredential(verificationId, otp)
    signInWithPhoneAuthCredential(credential)
  }

  private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
    _authStatus.postValue(AuthenticationStatus.OTPVerificationLoading)

    viewModelScope.launch {
      val signInResult = firebaseRepo.signInWithCredential(credential)
      _authStatus.postValue(
        AuthenticationStatus.OTPVerificationComplete(
          signInResult.isSuccessful,
          signInResult.exception
        )
      )
    }
  }

  fun sendVerificationEmail(email: String) {
    val actionCodeSettings = actionCodeSettings {
      // This must be true
      handleCodeInApp = true
      setAndroidPackageName(
        application.applicationContext.packageName,
        true, /* installIfNotAvailable */
        "1" /* minimumVersion */
      )
    }

    viewModelScope.launch {
      val emailVerificationResult = firebaseRepo.sendVerificationEmail(email, actionCodeSettings)
      _authStatus.postValue(
        AuthenticationStatus.EmailVerificationSent(
          emailVerificationResult.isSuccessful,
          emailVerificationResult.exception
        )
      )
    }
  }

  fun updateUser(fullName: String) {
    _authStatus.postValue(AuthenticationStatus.UserInfoUpdating)

    if (fullName.isNotBlank()) {
      viewModelScope.launch {
        val updateUserResult = firebaseRepo.updateUser(fullName)
        _authStatus.postValue(AuthenticationStatus.UserInfoUpdateComplete(updateUserResult))
      }
    } else {
      saveCurrentUserInFirestore()
    }
  }

  fun saveCurrentUserInFirestore() {
    viewModelScope.launch {
      firebaseRepo.isCurrentUserInFirestore()?.let { isCurrentUserInFirestore ->
        if (!isCurrentUserInFirestore) {
          firebaseRepo.createCurrentUserInFirestore { task ->
//            _authStatus.postValue(AuthenticationStatus.OTPVerificationComplete(task.isSuccessful, task.exception))
          }
        } else {
//          _authStatus.postValue(AuthenticationStatus.OTPVerificationSuccess)
        }
      }
    }
  }

  companion object {
    const val TAG = "AuthenticateViewModel"
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(AuthenticateViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return AuthenticateViewModel(app) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}

sealed class AuthenticationStatus {
  object PhoneVerificationSending : AuthenticationStatus()
  data class PhoneVerificationSent(val isSuccessful: Boolean) : AuthenticationStatus()

  object OTPVerificationLoading : AuthenticationStatus()
  data class OTPVerificationComplete(
    val isSuccessful: Boolean,
    val exception: Exception? = null
  ) : AuthenticationStatus()

  object EmailVerificationSending : AuthenticationStatus()
  data class EmailVerificationSent(
    val isSuccessful: Boolean,
    val exception: Exception? = null
  ) : AuthenticationStatus()

  object UserInfoUpdating : AuthenticationStatus()
  data class UserInfoUpdateComplete(val isSuccessful: Boolean) : AuthenticationStatus()
}