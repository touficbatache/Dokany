package com.batache.dokany.app.shop.stores

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.model.adapter.coverImage
import com.batache.dokany.util.DimensionUtils
import kotlinx.android.synthetic.main.activity_store_details.*
import kotlinx.android.synthetic.main.app_bar.*

class StoreDetailsActivity : BaseActivity() {

  private val viewModel: StoreDetailsViewModel by viewModels {
    StoreDetailsViewModel.Factory(
      application as DokanyApplication,
      intent.getStringExtra(INTENT_SELLER_ID) ?: ""
    )
  }

  private val controller: CoverImagesController by lazy { CoverImagesController() }

  override fun getLayout(): Int = R.layout.activity_store_details

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    coverImagesRv.setController(controller)

    viewModel.sellerDetails.observe(this, { seller ->
      supportActionBar?.title = seller.name

      controller.loadImages(seller.images)
      controller.requestModelBuild()

      seller.rating?.toFloat()?.let {
        starCountTv.text = "$it\nstars"
        ratingBar.rating = it
      }
      reviewCountTv.text = "(x reviews)"
      seller.productCount?.let { productCount ->
        val productCountName = if (productCount > 1) "products" else "product"
        productCountTv.text = "$productCount\n$productCountName"
      }
    })
  }

  inner class CoverImagesController : EpoxyController() {
    private val images: MutableList<String> = ArrayList()

    fun loadImages(images: List<String>) {
      this.images.clear()
      this.images.addAll(images)
    }

    override fun buildModels() {
      for (image in images) {
        coverImage {
          id(images.indexOf(image))
          imageUrl(image)
        }
      }
    }
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
//    if (initialToolbarHeight != 0) {
//      toolbar.updateLayoutParams { height = initialToolbarHeight + DimensionUtils.TOP_INSET }
//      toolbar.updatePadding(top = DimensionUtils.TOP_INSET)
//    }
  }

  companion object {
    private const val INTENT_SELLER_ID = "seller_id"

    fun newIntent(context: Context, sellerId: String): Intent {
      val intent = Intent(context, StoreDetailsActivity::class.java)
      intent.putExtra(INTENT_SELLER_ID, sellerId)
      return intent
    }
  }
}