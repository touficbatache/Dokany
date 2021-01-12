package com.batache.dokany.app.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.batache.dokany.util.DimensionUtils

abstract class BaseActivity : AppCompatActivity() {

  @LayoutRes
  abstract fun getLayout(): Int

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(getLayout())
  }

  override fun onResume() {
    super.onResume()

    WindowCompat.setDecorFitsSystemWindows(window, false)

//    setSystemUiVisibility()
    setInsetListener()
  }

//  protected open fun setSystemUiVisibility() {
//    val view = window.decorView
//    view.systemUiVisibility =
//        view.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//  }

  protected open fun setInsetListener() {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
      val systemWindowInsets = windowInsets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
      )
      DimensionUtils.LEFT_INSET = systemWindowInsets.left
      DimensionUtils.TOP_INSET = systemWindowInsets.top
      DimensionUtils.RIGHT_INSET = systemWindowInsets.right
      if (systemWindowInsets.bottom < resources.displayMetrics.heightPixels / 5) {
        DimensionUtils.BOTTOM_INSET = systemWindowInsets.bottom
      }
      onApplyAllWindowInsets()
      removeInsetListener()
      windowInsets
    }
  }

  protected open fun removeInsetListener() {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)
  }

  abstract fun onApplyAllWindowInsets()

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}