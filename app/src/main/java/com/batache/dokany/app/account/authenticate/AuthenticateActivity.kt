package com.batache.dokany.app.account.authenticate

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.transition.TransitionManager
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.formatPhoneNumberLBN
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.activity_authenticate.*
import kotlinx.android.synthetic.main.activity_authenticate_otp_screen.*
import kotlinx.android.synthetic.main.activity_authenticate_phone_number_screen.*
import kotlinx.android.synthetic.main.activity_authenticate_user_info_screen.*
import java.text.SimpleDateFormat
import java.util.*

class AuthenticateActivity : BaseActivity(), View.OnClickListener {

  private val viewModel: AuthenticateViewModel by viewModels {
    AuthenticateViewModel.Factory(application as DokanyApplication)
  }

  override fun getLayout(): Int = R.layout.activity_authenticate

  private val screenIds = listOf(R.id.phoneNumberScreen, R.id.otpScreen, R.id.userInfoScreen)
  private var currentScreenIndex = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    backBtn.setOnClickListener(this)

    initPhoneNumberScreen()
    initOtpScreen()
    initUserInfoScreen()

    viewModel.authStatus.observe(this) { status ->
      when (status) {
        AuthenticationStatus.PhoneVerificationSending -> {
          phoneNumberVerifyBtn.isEnabled = false
          phoneNumberVerifyLoadingView.visibility = View.VISIBLE
        }
        is AuthenticationStatus.PhoneVerificationSent -> {
          if (status.isSuccessful) {
            "+961${phoneNumberField.editText?.text}".formatPhoneNumberLBN().let { phoneNumber ->
              otpSubtitleTv.text = "Enter the verification code that has been sent to $phoneNumber"
            }

            nextScreen()
            otpView.requestFocus()
          } else {
            showSnackbar(R.string.error_invalid_phone_number)

            phoneNumberField.editText?.text?.clear()
            phoneNumberVerifyLoadingView.visibility = View.GONE
            phoneNumberVerifyBtn.isEnabled = false
          }
        }

        AuthenticationStatus.OTPVerificationLoading -> {
          otpNextBtn.isEnabled = false
          otpNextLoadingView.visibility = View.VISIBLE
        }
        is AuthenticationStatus.OTPVerificationComplete -> {
          if (status.isSuccessful) {
            nextScreen()
          } else {
            if (status.exception is FirebaseAuthInvalidCredentialsException) {
              // Incorrect verification code
              showSnackbar(R.string.error_incorrect_code)
            }

            otpView.text?.clear()
            otpNextLoadingView.visibility = View.GONE
            otpNextBtn.isEnabled = false
          }
        }

        AuthenticationStatus.EmailVerificationSending -> {
          userInfoDoneBtn.isEnabled = false
          userInfoDoneLoadingView.visibility = View.VISIBLE
        }
        is AuthenticationStatus.EmailVerificationSent -> {
          if (status.isSuccessful) {
            viewModel.updateUser(userNameField.editText?.text.toString())
          } else {
            showSnackbar(R.string.error_invalid_email)

            userEmailField.editText?.text?.clear()
            userInfoDoneLoadingView.visibility = View.GONE
            userInfoDoneBtn.isEnabled = false
          }
        }

        AuthenticationStatus.UserInfoUpdating -> {
          userInfoDoneBtn.isEnabled = false
          userInfoDoneLoadingView.visibility = View.VISIBLE
        }
        is AuthenticationStatus.UserInfoUpdateComplete -> {
          if (status.isSuccessful) {
            viewModel.saveCurrentUserInFirestore()
            finish()
          } else {
            showSnackbar(R.string.error_couldnt_save_info)
          }
        }
      }
    }
  }

  private fun nextScreen() {
    MaterialSharedAxis(MaterialSharedAxis.X, true).let { sharedAxis ->
      TransitionManager.beginDelayedTransition(rootView, sharedAxis)

      findViewById<View>(screenIds[currentScreenIndex]).visibility = View.GONE
      findViewById<View>(screenIds[currentScreenIndex + 1]).visibility = View.VISIBLE
    }

    currentScreenIndex++
  }

  private fun initPhoneNumberScreen() {
    phoneNumberField.editText?.doAfterTextChanged { phoneNumber ->
      phoneNumberVerifyBtn.isEnabled = phoneNumber?.length == 8
    }
    phoneNumberVerifyBtn.setOnClickListener(this)
  }

  private fun initOtpScreen() {
    otpView.setOtpCompletionListener {
      otpNextBtn.isEnabled = true
    }
    otpView.doAfterTextChanged { otp ->
      otp?.length?.let { otpLength ->
        if (otpLength < 6) {
          otpNextBtn.isEnabled = false
        }
      }
    }
    otpNextBtn.setOnClickListener(this)
  }

  private fun initUserInfoScreen() {
    userNameField.editText?.doAfterTextChanged { name ->
      userInfoDoneBtn?.isEnabled = !name.isNullOrBlank()
    }

    val items = listOf("Male", "Female", "Other", "Prefer not to say")
    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    (userGenderField.editText as? AutoCompleteTextView)?.setAdapter(adapter)

    userBirthdayField.editText?.setOnClickListener {
      val constraintsBuilder =
        CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())

      val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select your birthday")
        .setCalendarConstraints(constraintsBuilder.build())
        .build()

      datePicker.addOnPositiveButtonClickListener { date ->
        userBirthdayField.editText?.setText(
          SimpleDateFormat("MMM d, y", Locale.getDefault())
            .format(Date(date))
            .toString()
        )
      }

      datePicker.show(supportFragmentManager, "MaterialDatePicker")
    }

    userInfoDoneBtn.setOnClickListener(this)
  }

  override fun onClick(view: View?) {
    when (view?.id) {
      R.id.backBtn -> finish()
      R.id.phoneNumberVerifyBtn -> {
        viewModel.sendVerificationOtp("+961", phoneNumberField.editText?.text.toString(), this)
      }
      R.id.otpNextBtn -> {
        otpView.text.toString().let { otp ->
          if (otp.isNotEmpty()) viewModel.signInWithOtp(otp)
        }
      }
      R.id.userInfoDoneBtn -> {
//        val userEnteredEmail = userEmailField.editText.toString()
//        if (userEnteredEmail.isNullOrBlank()) {
          viewModel.updateUser(userNameField.editText?.text.toString())
//        } else {
//          viewModel.sendVerificationEmail(userEnteredEmail)
//        }
      }
    }
  }

  private fun showSnackbar(message: Int) {
    Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
  }

  override fun onApplyAllWindowInsets() {
    rootView.updatePadding(
      top = DimensionUtils.TOP_INSET,
      bottom = if (DimensionUtils.KEYBOARD_INSET == 0)
        DimensionUtils.BOTTOM_INSET
      else
        DimensionUtils.KEYBOARD_INSET
    )
  }

  override fun onDestroy() {
    super.onDestroy()

    backBtn.setOnClickListener(null)

    phoneNumberVerifyBtn.setOnClickListener(null)
    otpNextBtn.setOnClickListener(null)
    userInfoDoneBtn.setOnClickListener(null)
  }
}