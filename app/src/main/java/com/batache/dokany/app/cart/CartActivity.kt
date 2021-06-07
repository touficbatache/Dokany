package com.batache.dokany.app.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.app.cart.checkout.CheckoutActivity
import com.batache.dokany.app.shop.products.product_details.ProductDetailsActivity
import com.batache.dokany.calculateSubtotal
import com.batache.dokany.model.adapter.cartProduct
import com.batache.dokany.model.pojo.cart.CartProduct
import com.batache.dokany.util.Constants
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.app_bar.*

class CartActivity : BaseActivity(), View.OnClickListener {

  private val viewModel: CartViewModel by viewModels {
    CartViewModel.Factory(application as DokanyApplication)
  }

  private var homeResultLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Constants.GO_HOME) {
        finish()
      }
    }

  private lateinit var controller: CartProductsController

  override fun getLayout(): Int = R.layout.activity_cart

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    controller = CartProductsController()
    cartProductsRv.setController(controller)

    viewModel.cartProducts.observe(this, { cartProducts ->
      controller.setProducts(cartProducts)
      controller.requestModelBuild()

      cartSubtotalTv.price = cartProducts.calculateSubtotal()

      showEmptyCartLayout(cartProducts.isEmpty())
      // Add them to a recyclerview
    })

    checkoutBtn.setOnClickListener(this)
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
      R.id.checkoutBtn -> {
        if (viewModel.isUserSignedIn) {
          homeResultLauncher.launch(Intent(this, CheckoutActivity::class.java))
        } else {
          onAuthError()
        }
      }
    }
  }

  private fun onAuthError() {
    showSnackbar(R.string.checkout_auth_error, R.string.sign_in) {
      val intent = Intent()
      setResult(Constants.GO_TO_AUTH_RESULT_CODE, intent)
      finish()
    }
  }

  private fun showSnackbar(
    @StringRes message: Int,
    @StringRes actionText: Int = -1,
    actionClickListener: View.OnClickListener? = null
  ) {
    Snackbar
      .make(rootView, message, Snackbar.LENGTH_LONG)
      .setAnchorView(checkoutContainer)
      .setAction(actionText, actionClickListener)
      .show()
  }

  inner class CartProductsController : EpoxyController() {
    private val products: MutableList<CartProduct> = ArrayList()

    fun setProducts(cartProducts: List<CartProduct>) {
      products.clear()
      products.addAll(cartProducts)
    }

    override fun buildModels() {
      for (cartProduct in products) {
        cartProduct {
          id(cartProduct.productId)
          quantity(cartProduct.quantity)
          images(cartProduct.productImages)
          title(cartProduct.productTitle)
          relevantDetail(cartProduct.productRelevantDetail)
          unitPrice(cartProduct.productSellers?.find { it.id == cartProduct.sellerId }?.price)
          listener(View.OnClickListener {
            startActivity(
              ProductDetailsActivity.newIntent(
                this@CartActivity,
                cartProduct.productId
              )
            )
          })
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