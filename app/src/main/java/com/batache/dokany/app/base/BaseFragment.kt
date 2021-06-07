package com.batache.dokany.app.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.batache.dokany.app.MainActivity
import com.batache.dokany.app.account.authenticate.AuthenticateActivity
import com.batache.dokany.util.Constants
import com.batache.dokany.util.DimensionUtils

abstract class BaseFragment : Fragment() {

  val activity: AppCompatActivity? get() = getActivity() as AppCompatActivity?

  val mainActivity: MainActivity? get() = if (getActivity() is MainActivity) getActivity() as MainActivity else null

  protected var authResultLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Constants.GO_TO_AUTH_RESULT_CODE) {
        startActivity(Intent(requireContext(), AuthenticateActivity::class.java))
      }
    }

  lateinit var rootView: View

  @LayoutRes
  abstract fun getLayout(): Int

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    rootView = inflater.inflate(getLayout(), container, false)
    onApplyAllWindowInsets()
    return rootView
  }

  override fun onResume() {
    super.onResume()

    setInsetListener()
    rootView.requestApplyInsets()
  }

  protected open fun setInsetListener() {
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
      val systemWindowInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
      DimensionUtils.LEFT_INSET = systemWindowInsets.left
      DimensionUtils.TOP_INSET = systemWindowInsets.top
      DimensionUtils.RIGHT_INSET = systemWindowInsets.right
//      if (systemWindowInsets.bottom < resources.displayMetrics.heightPixels / 5) {
      DimensionUtils.BOTTOM_INSET = systemWindowInsets.bottom
//      }

      val keyboardInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
      DimensionUtils.KEYBOARD_INSET = keyboardInsets.bottom

      onApplyAllWindowInsets()
      windowInsets
    }
  }

  open fun onApplyAllWindowInsets() {

  }

  override fun onPause() {
    super.onPause()

    removeInsetListener()
  }

  protected open fun removeInsetListener() {
    ViewCompat.setOnApplyWindowInsetsListener(rootView, null)
  }
}