package com.batache.dokany.app

import android.os.Bundle
import androidx.core.view.updatePadding
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.util.DimensionUtils
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.app_bar.*

class AddProductActivity : BaseActivity() {

  override fun getLayout(): Int = R.layout.activity_add_product

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    addBtn.setOnClickListener {

    }
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    contentLl.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }
}