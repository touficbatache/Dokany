package com.batache.dokany.app.shop.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.view.updatePadding
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.util.DimensionUtils
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : BaseActivity(), TextWatcher {

  override fun getLayout(): Int = R.layout.activity_search

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    backBtn.setOnClickListener {
      finish()
    }
    searchEt.addTextChangedListener(this)
  }

  override fun onResume() {
    super.onResume()

    searchEt.requestFocus()
//    val controller = searchEt.windowInsetsController
//    controller.show(WindowInsetsCompat.Type.ime())
    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(searchEt, InputMethodManager.SHOW_IMPLICIT)

//    searchEt.requestFocus()
//    val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputMethodManager.showSoftInput(searchEt, InputMethodManager.SHOW_IMPLICIT)
  }

  override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

  override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

  override fun afterTextChanged(query: Editable?) {
    performSearch(query.toString())
  }

  private fun performSearch(query: String) {

  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
  }

  override fun onDestroy() {
    super.onDestroy()

    backBtn.setOnClickListener(null)
    searchEt.removeTextChangedListener(this)
  }
}