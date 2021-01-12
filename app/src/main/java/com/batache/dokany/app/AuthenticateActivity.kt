package com.batache.dokany.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.transition.TransitionManager
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.util.DimensionUtils
import com.batache.dokany.util.StringUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.activity_authenticate.*

class AuthenticateActivity : BaseActivity(), View.OnClickListener {

  private val viewModel: AuthenticateViewModel by viewModels()

  private val otp: String
    get() {
      return smsVerificationOtpView.text.toString()
    }

  override fun getLayout(): Int = R.layout.activity_authenticate

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    backBtn.setOnClickListener(this)

    phoneNumberEt.doAfterTextChanged { phoneNumber ->
      sendVerificationCodeBtn.isEnabled = phoneNumber?.length == 8
    }
    sendVerificationCodeBtn.setOnClickListener(this)

    smsVerificationOtpView.setOtpCompletionListener {
      signInBtn.isEnabled = true
    }
    smsVerificationOtpView.doAfterTextChanged { otp ->
      otp?.length?.let { otpLength ->
        if (otpLength < 6) {
          signInBtn.isEnabled = false
        }
      }
    }
    signInBtn.setOnClickListener(this)

    viewModel.phoneAuthStatus.observe(this, Observer { status ->
      when (status) {
        PhoneAuthStatus.PhoneVerificationLoading -> {
          sendVerificationCodeBtn.visibility = View.GONE
          sendVerificationCodeLoadingView.visibility = View.VISIBLE
        }
        PhoneAuthStatus.PhoneVerificationSuccess -> {

          StringUtils.formatLebanesePhoneNumber("+961${phoneNumberEt.text}").let { phoneNumber ->
            smsVerificationSubtitleTv.text =
              "Enter the verification code that has been sent to\n$phoneNumber"
          }

          MaterialSharedAxis(MaterialSharedAxis.X, true).let { sharedAxis ->
            TransitionManager.beginDelayedTransition(subtitles as ViewGroup, sharedAxis)

            phoneNumberSubtitleTv.visibility = View.GONE
            smsVerificationSubtitleTv.visibility = View.VISIBLE

            smsVerificationOtpView.requestFocus()
          }

          MaterialSharedAxis(MaterialSharedAxis.X, true).let { sharedAxis ->
            TransitionManager.beginDelayedTransition(steps as ViewGroup, sharedAxis)

            phoneNumberStep.visibility = View.GONE
            smsVerificationStep.visibility = View.VISIBLE

            smsVerificationOtpView.requestFocus()
          }
        }
        PhoneAuthStatus.PhoneVerificationFailed -> {
          showSnackbar(R.string.error_invalid_phone_number)

          phoneNumberEt.text?.clear()
          sendVerificationCodeLoadingView.visibility = View.GONE
          sendVerificationCodeBtn.visibility = View.VISIBLE
        }
        PhoneAuthStatus.OTPVerificationLoading -> {
          signInBtn.visibility = View.GONE
          signInLoadingView.visibility = View.VISIBLE
        }
        PhoneAuthStatus.OTPVerificationSuccess -> {
          finish()
        }
        is PhoneAuthStatus.OTPVerificationFailed -> {
          if (status.exception is FirebaseAuthInvalidCredentialsException) {
            // Incorrect verification code
            showSnackbar(R.string.error_incorrect_code)
          }

          smsVerificationOtpView.text?.clear()
          signInLoadingView.visibility = View.GONE
          signInBtn.visibility = View.VISIBLE
        }
      }
    })
  }

  override fun onClick(view: View?) {
    when (view?.id) {
      R.id.backBtn -> finish()
      R.id.sendVerificationCodeBtn -> sendVerificationCode()
      R.id.signInBtn -> signIn()
    }
  }

  private fun sendVerificationCode() {
    viewModel.sendVerificationCode("+961", phoneNumberEt.text.toString(), this)
  }

  private fun signIn() {
    if (otp.isNotEmpty()) {
      viewModel.signInWithOtp(otp)
    }
  }

  private fun showSnackbar(message: Int) {
    Snackbar.make(window.decorView.rootView, message, Snackbar.LENGTH_SHORT).show()
  }

  override fun onApplyAllWindowInsets() {
    rootView.updatePadding(top = DimensionUtils.TOP_INSET, bottom = DimensionUtils.BOTTOM_INSET)
  }

  override fun onDestroy() {
    super.onDestroy()

    backBtn.setOnClickListener(null)
    sendVerificationCodeBtn.setOnClickListener(null)
    signInBtn.setOnClickListener(null)
  }
}