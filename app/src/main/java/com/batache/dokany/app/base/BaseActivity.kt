package com.batache.dokany.app.base

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.batache.dokany.app.account.authenticate.AuthenticateActivity
import com.batache.dokany.util.Constants
import com.batache.dokany.util.DimensionUtils

abstract class BaseActivity : AppCompatActivity() {

  protected var authResultLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Constants.GO_TO_AUTH_RESULT_CODE) {
        startActivity(Intent(this, AuthenticateActivity::class.java))
      }
    }

  @LayoutRes
  abstract fun getLayout(): Int

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(getLayout())
  }

  override fun onResume() {
    super.onResume()

    WindowCompat.setDecorFitsSystemWindows(window, false)
    setInsetListener()
  }

  protected open fun setInsetListener() {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
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

  abstract fun onApplyAllWindowInsets()

  override fun onPause() {
    super.onPause()

    removeInsetListener()
  }

  protected open fun removeInsetListener() {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}