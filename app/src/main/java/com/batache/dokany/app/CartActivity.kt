package com.batache.dokany.app

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.model.adapter.CartProductModel_
import com.batache.dokany.model.pojo.CartProductDetails
import com.batache.dokany.util.DimensionUtils
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.app_bar.*

class CartActivity : BaseActivity(), View.OnClickListener {

  private val viewModel: CartViewModel by viewModels {
    CartViewModelFactory(
      (application as DokanyApplication).cartProductsRepository
    )
  }

  private lateinit var controller: CartProductsController

  override fun getLayout(): Int = R.layout.activity_cart

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    controller = CartProductsController()
    cartProductsRv.setController(controller)

    viewModel.cartProductsDetails.observe(this, Observer { cartProducts ->
      controller.setProducts(cartProducts)
      controller.requestModelBuild()

      showEmptyCartLayout(cartProducts.isEmpty())
      // Add them to a recyclerview
    })


  }

  private fun showEmptyCartLayout(isEmpty: Boolean) {
    if (isEmpty) {
      emptyCartLl.visibility = View.VISIBLE
      content.visibility = View.GONE
      continueShoppingBtn.setOnClickListener(this)
    } else {
      emptyCartLl.visibility = View.GONE
      content.visibility = View.VISIBLE
      continueShoppingBtn.setOnClickListener(null)
    }
  }

  override fun onClick(view: View?) {
    when (view?.id) {
      R.id.continueShoppingBtn -> finish()
    }
  }

  inner class CartProductsController : EpoxyController() {
    private val products: MutableList<CartProductDetails> = ArrayList()

    fun setProducts(cartProducts: List<CartProductDetails>) {
      products.clear()
      products.addAll(cartProducts)
    }

    override fun buildModels() {
      for (cartProduct in products) {
        CartProductModel_().apply {
          id(cartProduct.productId)
          quantity(cartProduct.quantity)
          images(cartProduct.productImages)
          title(cartProduct.productTitle)
          relevantDetail(cartProduct.productRelevantDetail)
          unitPrice(cartProduct.productPrice)
          listener(View.OnClickListener {
            startActivity(ProductDetailActivity.newIntent(this@CartActivity, cartProduct.productId))
          })
          addTo(this@CartProductsController)
        }
      }
    }

  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    contentCl.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }

  override fun onDestroy() {
    super.onDestroy()

    continueShoppingBtn.setOnClickListener(null)
  }
}