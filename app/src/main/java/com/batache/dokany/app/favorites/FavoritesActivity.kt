package com.batache.dokany.app.favorites

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.app.shop.products.product_details.ProductDetailsActivity
import com.batache.dokany.model.adapter.ProductTapListener
import com.batache.dokany.model.adapter.product
import com.batache.dokany.model.pojo.product.Product
import com.batache.dokany.util.DimensionUtils
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.android.synthetic.main.fragment_favorites.*

class FavoritesActivity : BaseActivity() {

  private val viewModel: FavoritesViewModel by viewModels {
    FavoritesViewModel.Factory(application as DokanyApplication)
  }

  private lateinit var controller: FavoritesController

  override fun getLayout(): Int = R.layout.fragment_favorites

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    controller = FavoritesController()
    favoritesRv.setController(controller)

    viewModel.productsInFavorites.observe(this) { productsInFavorites ->
      if (!productsInFavorites.isNullOrEmpty()) {
        controller.setFavoritedProducts(productsInFavorites)
        controller.requestModelBuild()
        emptyFavoritesLl.visibility = View.GONE
      } else {
        emptyFavoritesLl.visibility = View.VISIBLE
      }
    }

    continueShoppingBtn.setOnClickListener {
      finish()
    }

    viewModel.eventFirestoreError.observe(this) { isFirestoreError ->
      if (isFirestoreError) onFirestoreError()
    }
  }

  inner class FavoritesController : EpoxyController() {
    private val favoritedProducts: MutableList<Product> = ArrayList()

    fun setFavoritedProducts(products: List<Product>) {
      favoritedProducts.clear()
      favoritedProducts.addAll(products)
    }

    override fun buildModels() {
      for (product in favoritedProducts) {
        product {
          id(product.id)
          withFavoriteLayout()
          product(product)
          listener(object : ProductTapListener {
            override fun onSingleClick(productId: String) {
              startActivity(
                ProductDetailsActivity.newIntent(
                  this@FavoritesActivity,
                  product.id
                )
              )
            }

            override fun onDoubleClick(productId: String) {}
            override fun onAddToCartClick(productId: String, productSellerIds: List<String>) {}
          })
        }
      }
    }
  }

  private fun onFirestoreError() {
    if (!viewModel.isFirestoreErrorShown.value!!) {
      emptyFavoritesLl.visibility = View.VISIBLE
      viewModel.onFirestoreErrorShown()
    }
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    contentFl.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }
}