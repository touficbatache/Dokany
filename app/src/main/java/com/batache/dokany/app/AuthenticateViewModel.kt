package com.batache.dokany.app

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batache.dokany.model.pojo.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthenticateViewModel : ViewModel() {

  val phoneAuthStatus = MutableLiveData<PhoneAuthStatus>()

  private var verificationId: String = ""

  fun sendVerificationCode(countryCode: String, phoneNumber: String, activity: AuthenticateActivity) {
    phoneAuthStatus.postValue(PhoneAuthStatus.PhoneVerificationLoading)

    val mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
      object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
          super.onCodeSent(verificationId, token)

          this@AuthenticateViewModel.verificationId = verificationId
          phoneAuthStatus.postValue(PhoneAuthStatus.PhoneVerificationSuccess)
        }

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
          signInWithPhoneAuthCredential(phoneAuthCredential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
          phoneAuthStatus.postValue(PhoneAuthStatus.PhoneVerificationFailed)
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
    viewModelScope.launch {
      phoneAuthStatus.postValue(PhoneAuthStatus.OTPVerificationLoading)

      FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
        if (task.isSuccessful) {
          // Sign in success
          task.result?.user?.let { user ->
            saveUserInFirestore(user)
          }
        } else {
          // Sign in failed
          Log.e(TAG, "signInWithCredential:failure", task.exception)

          task.exception?.let { exception ->
            phoneAuthStatus.postValue(PhoneAuthStatus.OTPVerificationFailed(exception))
          }
        }
      }
    }
  }

  private fun saveUserInFirestore(user: FirebaseUser) {
    viewModelScope.launch {
      val document = Firebase.firestore.collection("users").document(user.uid).get().await()
      if (!document.exists()) {
        Firebase.firestore.collection("users").document(user.uid).set(
          User(
            fullName = user.displayName ?: ""
          )
        ).addOnSuccessListener {
          phoneAuthStatus.postValue(PhoneAuthStatus.OTPVerificationSuccess)
        }.addOnFailureListener {
          phoneAuthStatus.postValue(PhoneAuthStatus.OTPVerificationFailed(it))
        }
      } else {
        phoneAuthStatus.postValue(PhoneAuthStatus.OTPVerificationSuccess)
      }
    }
  }

  companion object {
    const val TAG = "AuthenticateViewModel"
  }

}

sealed class PhoneAuthStatus {
  object PhoneVerificationLoading : PhoneAuthStatus()
  object PhoneVerificationSuccess : PhoneAuthStatus()
  object PhoneVerificationFailed : PhoneAuthStatus()
  object OTPVerificationLoading : PhoneAuthStatus()
  object OTPVerificationSuccess : PhoneAuthStatus()
  data class OTPVerificationFailed(val exception: Exception) : PhoneAuthStatus()
}