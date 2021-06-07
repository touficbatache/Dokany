package com.batache.dokany.app.shop.products.product_details

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Observer
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.*
import com.batache.dokany.app.base.LoadingActivity
import com.batache.dokany.app.cart.CartActivity
import com.batache.dokany.app.shop.products.QuickAddToCartDialog
import com.batache.dokany.model.adapter.ProductTapListener
import com.batache.dokany.model.adapter.product
import com.batache.dokany.model.pojo.product.Product
import com.batache.dokany.util.Constants
import com.batache.dokany.util.DimensionUtils
import com.batache.dokany.view.QuantityView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_product_details.*
import kotlinx.android.synthetic.main.app_bar.*

class ProductDetailsActivity : LoadingActivity(), View.OnClickListener {

  private val viewModel: ProductDetailsViewModel by viewModels {
    ProductDetailsViewModel.Factory(
      application as DokanyApplication,
      intent.getStringExtra(KEY_PRODUCT_ID) ?: ""
    )
  }

  private var showGoToCart: Boolean = false
    set(value) {
      field = value

      addOrGoToCartBtn.apply {
        if (value) {
          text = resources.getString(R.string.go_to_cart)
          setIconResource(R.drawable.ic_solid_shopping_cart)
          visibility = View.VISIBLE
        } else {
          text = resources.getString(R.string.add_to_cart)
          setIconResource(R.drawable.ic_line_shopping_cart_add)
        }
      }
    }

  private val quantity get() = quantityChooserView.quantity
  private lateinit var relatedProductsController: RelatedProductsController

  override fun getLayout(): Int = R.layout.activity_product_details

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    topSectionLl.doOnLayout {
      val productDetailsLayoutParams = productDetailsCard.layoutParams
      if (productDetailsLayoutParams is CoordinatorLayout.LayoutParams &&
        productDetailsLayoutParams.behavior is BottomSheetBehavior
      ) {
        val behavior = BottomSheetBehavior.from(productDetailsCard)
        behavior.peekHeight = resources.displayMetrics.heightPixels - topSectionLl.measuredHeight
      }
    }

    backBtn.setOnClickListener(this)

    topSectionLl.setOnClickListener(object : DoubleClickListener() {
      override fun onSingleClick(view: View?) {}

      override fun onDoubleClick(view: View?) {
        viewModel.addToFavorites()
        favoriteAnim.alpha = 0.9F
        val drawable = AnimatedVectorDrawableCompat.create(
          favoriteAnim.context, R.drawable.avd_favorite
        )
        favoriteAnim.setImageDrawable(drawable)
        drawable?.start()
      }
    })

    favoriteBtn.setOnClickListener(this)

    sellerChooserView.onReset = {
      // Add to cart will automatically replace
      // the current product with the new seller ID
      // since the primary key is the product ID
      viewModel.addToCart(sellerChooserView.selectedSellerId, quantity)
    }

    relatedProductsController = RelatedProductsController()
    relatedProductsRv.setController(relatedProductsController)

    viewModel.product.observe(this, { product ->
      product?.let {
        loadProductDetails(product)
      }
    })

//    viewModel.productSellers.observe(this, { productSellers ->
//      loadPrices(productSellers)
//    })

    viewModel.relatedProducts.observe(this, { relatedProducts ->
      relatedProductsController.setProducts(relatedProducts)
      relatedProductsController.requestModelBuild()
    })

    viewModel.cartEvent.observe(this, Observer {
      showGoToCart = false

      addOrGoToCartBtn.visibility = View.GONE
      removeFromCartBtn.visibility = View.GONE
      changeQuantityBtn.visibility = View.GONE

      when (it) {
        is ProductDetailsViewModel.CartEvent.SyncComplete -> {
          addOrGoToCartBtn.visibility = View.VISIBLE
          if (it.isInCart) {
            showGoToCart = true
            sellerChooserView.isResetRequired = true
            sellerChooserView.selectedSellerId = viewModel.cartSellerId
            quantityChooserView.min = 0
            quantityChooserView.quantity = viewModel.quantityInCart
          } else {
            quantityChooserView.min = 1
          }
        }
        ProductDetailsViewModel.CartEvent.AddedToCart -> {
          showGoToCart = true
          val drawable = AnimatedVectorDrawableCompat.create(
            this, R.drawable.avd_add_to_shopping_cart
          )
          addOrGoToCartBtn.icon = drawable
          drawable?.start()

          quantityChooserView.min = 0
          quantityChooserView.quantity = viewModel.quantityInCart
        }
        ProductDetailsViewModel.CartEvent.RemovedFromCart -> {
          addOrGoToCartBtn.visibility = View.VISIBLE
          quantityChooserView.min = 1
          finish()
        }
        ProductDetailsViewModel.CartEvent.QuantityModified -> {
          if (viewModel.quantityInCart == 0) {
            addOrGoToCartBtn.visibility = View.VISIBLE
            return@Observer
          }

          if (quantityChooserView.quantity > 0) {
            if (quantityChooserView.quantity == viewModel.quantityInCart) {
              showGoToCart = true
            } else {
              changeQuantityBtn.visibility = View.VISIBLE
            }
          } else {
            removeFromCartBtn.visibility = View.VISIBLE
          }
        }
        ProductDetailsViewModel.CartEvent.QuantityChanged -> {
          showGoToCart = true
          quantityChooserView.min = 0
        }
      }
    })

    viewModel.isInFavoritesLive().observe(this, {
      favoriteBtn.isSelected = it != null
    })

    viewModel.eventNetworkError.observe(this, { isNetworkError ->
      if (isNetworkError) onNetworkError()
    })

    viewModel.eventAuthError.observe(this, { isAuthError ->
      if (isAuthError) onAuthError()
    })
  }

  override fun onClick(view: View?) {
    when (view?.id) {
      R.id.backBtn -> finish()
      R.id.favoriteBtn -> {
        if (!favoriteBtn.isSelected) {
          viewModel.addToFavorites()
        } else {
          viewModel.removeFromFavorites()
        }
      }
    }
  }

  private fun loadProductDetails(product: Product) {
    supportActionBar?.title = product.title

    product.images[0].let { productImage ->
      GlideApp.with(this)
        .load(FirebaseStorage.getInstance().getReferenceFromUrl(productImage))
        .extractColors { colors ->
          colors?.apply {
            backgroundCl.setBackgroundColor(colors.backgroundColor)
            ImageViewCompat.setImageTintList(
              favoriteBtn,
              ColorStateList.valueOf(colors.darkerColor)
            )
            relevantDetailTv.setTextColor(colors.darkerColor)
            addOrGoToCartBtn.backgroundTintList = ColorStateList.valueOf(colors.darkerColor)
            changeQuantityBtn.backgroundTintList = ColorStateList.valueOf(colors.darkerColor)
            removeFromCartBtn.strokeColor = ColorStateList.valueOf(colors.darkerColor)
            removeFromCartBtn.setTextColor(ColorStateList.valueOf(colors.darkerColor))
            removeFromCartBtn.rippleColor = ColorStateList.valueOf(colors.darkerColor)
          }
          onLoaded()
        }
        .into(imageView)
    }

    nameTv.text = product.title
    relevantDetailTv.text = product.relevantDetail

    if (product.description.isNotEmpty()) {
      descriptionTv.visibility = View.VISIBLE
      descriptionTv.text = product.description
    }

    if (!product.sellers.isNullOrEmpty()) {
      sellerChooserView.sellers = product.sellers
      sellerChooserView.visibility = View.VISIBLE
      quantityChooserView.visibility = View.VISIBLE
      buttonsContainer.visibility = View.VISIBLE
    }

    quantityChooserView.setOnQuantityChangeListener(object : QuantityView.OnQuantityChangeListener {
      override fun onChange(oldQuantity: Int, newQuantity: Int) {
        viewModel.onCartQuantityModified()
      }
    })

    addOrGoToCartBtn.setOnClickListener {
      if (!showGoToCart) {
        sellerChooserView.isResetRequired = true
        viewModel.addToCart(sellerChooserView.selectedSellerId, quantity)
      } else {
        startActivity(Intent(this, CartActivity::class.java))
      }
    }
    removeFromCartBtn.setOnClickListener {
      viewModel.removeFromCart()
    }
    changeQuantityBtn.setOnClickListener {
      viewModel.updateCartQuantity(quantity)
    }
  }

  private fun onNetworkError() {
    if (!viewModel.isNetworkErrorShown.value!!) {
      showSnackbar(R.string.network_error)
      viewModel.onNetworkErrorShown()
    }
  }

  private fun onAuthError() {
    showSnackbar(R.string.auth_error, R.string.sign_in) {
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
    val rootView: View = rootViewCoordLayout ?: rootViewConstLayout

    Snackbar
      .make(rootView, message, Snackbar.LENGTH_SHORT)
      .setAnchorView(bottomActionsLl)
      .setAction(actionText, actionClickListener)
      .apply {
        view.elevation = 100.px.toFloat()
      }
      .show()
  }

  override fun onApplyAllWindowInsets() {
    backgroundCl.updatePadding(top = DimensionUtils.TOP_INSET)

    if (productDetailsCard.layoutParams !is CoordinatorLayout.LayoutParams) {
      productDetailsCl.updatePadding(top = DimensionUtils.TOP_INSET)
    }
    productDetailsCl.updateMargin(bottom = DimensionUtils.BOTTOM_INSET)

    bottomActionsLl.updateMargin(bottom = DimensionUtils.BOTTOM_INSET)
  }

  override fun onDestroy() {
    super.onDestroy()

    addOrGoToCartBtn.setOnClickListener(null)
    removeFromCartBtn.setOnClickListener(null)
    changeQuantityBtn.setOnClickListener(null)
  }

  inner class RelatedProductsController : EpoxyController() {
    private val products: MutableList<Product> = ArrayList()

    fun setProducts(products: List<Product>) {
      this.products.clear()
      this.products.addAll(products)
    }

    override fun buildModels() {
      for (product in products) {
        product {
          id(product.id)
          withSlideLayout()
          product(product)
          listener(object : ProductTapListener {
            override fun onSingleClick(productId: String) {
              finish()
              startActivity(newIntent(this@ProductDetailsActivity, productId))
            }

            override fun onDoubleClick(productId: String) {
              viewModel.addToFavorites(productId)
            }

            override fun onAddToCartClick(productId: String, productSellerIds: List<String>) {
              QuickAddToCartDialog.newInstance(
                productId,
                ArrayList(productSellerIds)
              ).show(supportFragmentManager, QuickAddToCartDialog.TAG)
            }
          })
        }
      }
    }
  }

  companion object {
    private const val KEY_PRODUCT_ID = "KEY_PRODUCT_ID"

    fun newIntent(context: Context, productId: String): Intent {
      val intent = Intent(context, ProductDetailsActivity::class.java)
      intent.putExtra(KEY_PRODUCT_ID, productId)
      return intent
    }
  }
}