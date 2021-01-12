package com.batache.dokany.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.batache.dokany.DokanyApplication
import com.batache.dokany.DokanySimpleAdapter
import com.batache.dokany.GlideApp
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.model.pojo.Product
import com.batache.dokany.model.pojo.ProductSellerDetails
import com.batache.dokany.util.DimensionUtils
import com.batache.dokany.view.QuantityView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.app_bar.*

class ProductDetailActivity : BaseActivity() {

  private val viewModel: ProductDetailViewModel by viewModels {
    ProductDetailViewModelFactory(
      intent.getStringExtra(INTENT_PRODUCT_ID) ?: "",
      (application as DokanyApplication).productsRepository,
      (application as DokanyApplication).productSellersRepository,
      (application as DokanyApplication).favoritesRepository,
      (application as DokanyApplication).cartProductsRepository
    )
  }

  private var selectedSellerId: String? = null

  private val quantity get() = quantityChooserView.quantity

  override fun getLayout(): Int = R.layout.activity_product_detail

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    contentCl.visibility = View.GONE

    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setDisplayShowTitleEnabled(false)

    viewModel.productDetails.observe(this, Observer { product ->
      loadProductDetails(product)
    })

    viewModel.productSellers.observe(this, Observer { productSellers ->
      loadPrices(productSellers)
    })

    viewModel.cartEvent.observe(this, Observer {
      addToCartBtn.visibility = View.GONE
      goToCartBtn.visibility = View.GONE
      removeFromCartBtn.visibility = View.GONE
      changeQuantityBtn.visibility = View.GONE

      when (it) {
        is ProductDetailViewModel.CartEvent.SyncComplete -> {
          if (it.isInCart) {
            goToCartBtn.visibility = View.VISIBLE
            quantityChooserView.min = 0
            quantityChooserView.quantity = viewModel.quantityInCart
          } else {
            addToCartBtn.visibility = View.VISIBLE
            quantityChooserView.min = 1
          }
        }
        ProductDetailViewModel.CartEvent.AddedToCart -> {
          goToCartBtn.visibility = View.VISIBLE
          quantityChooserView.min = 0
          quantityChooserView.quantity = viewModel.quantityInCart
          showSnackbar(R.string.product_added_to_cart)
        }
        ProductDetailViewModel.CartEvent.RemovedFromCart -> {
          addToCartBtn.visibility = View.VISIBLE
          quantityChooserView.min = 1
          finish()
        }
        ProductDetailViewModel.CartEvent.QuantityModified -> {
          if (viewModel.quantityInCart == 0) {
            addToCartBtn.visibility = View.VISIBLE
            return@Observer
          }

          if (quantityChooserView.quantity > 0) {
            if (quantityChooserView.quantity == viewModel.quantityInCart) {
              goToCartBtn.visibility = View.VISIBLE
            } else {
              changeQuantityBtn.visibility = View.VISIBLE
            }
          } else {
            removeFromCartBtn.visibility = View.VISIBLE
          }
        }
        ProductDetailViewModel.CartEvent.QuantityChanged -> {
          goToCartBtn.visibility = View.VISIBLE
          quantityChooserView.min = 0
          showSnackbar(R.string.product_quantity_changed)
        }
      }
    })
  }

  private fun loadProductDetails(product: Product) {
    supportActionBar?.title = product.title

    product.images[0].let { productImage ->
      GlideApp.with(this)
        .load(FirebaseStorage.getInstance().getReferenceFromUrl(productImage))
        .into(imageView)
    }

    titleTv.text = product.title
    relevantDetailTv.text = product.relevantDetail

    quantityChooserView.setOnQuantityChangeListener(object : QuantityView.OnQuantityChangeListener {
      override fun onChange(oldQuantity: Int, newQuantity: Int) {
        viewModel.cartEvent.postValue(ProductDetailViewModel.CartEvent.QuantityModified)
      }
    })

    addToCartBtn.setOnClickListener {
      viewModel.addToCart(selectedSellerId, quantity)
    }
    goToCartBtn.setOnClickListener {
      startActivity(Intent(this, CartActivity::class.java))
    }
    removeFromCartBtn.setOnClickListener {
      viewModel.removeFromCart()
    }
    changeQuantityBtn.setOnClickListener {
      viewModel.updateCartQuantity(quantity)
      showSnackbar(R.string.product_quantity_changed)
    }

    contentCl.visibility = View.VISIBLE
  }

  private fun loadPrices(productSellers: List<ProductSellerDetails>) {
    if (productSellers.isNullOrEmpty()) {
      return
    }

    sellerChooserView.sellers = productSellers

    val adapterSellers: MutableList<Map<String, String>> = ArrayList()

    for (productSeller in productSellers) {
      if (productSeller.price != null) {
        val datum: MutableMap<String, String> = HashMap()
        datum["id"] = productSeller.id
        datum["price"] = "${productSeller.price} LBP"
        datum["seller_rating"] =
          "at ${productSeller.name} \u2022 ${productSeller.starsRating} stars (${(113..147).random()} reviews)"
        adapterSellers.add(datum)
      }
    }

    val adapter = DokanySimpleAdapter(
      this,
      adapterSellers,
      R.layout.layout_seller_price,
      arrayOf("price", "seller_rating"),
      intArrayOf(R.id.priceTv, R.id.sellerAndRatingTv)
    )

    val autoCompleteTextView = pricesMenu.editText as? AutoCompleteTextView
    autoCompleteTextView?.apply {
      setAdapter(adapter)
      selectedSellerId = productSellers[0].id
      setText("${productSellers[0].price} LBP", false)
      onItemClickListener =
        OnItemClickListener { parent, view, position, id ->
          selectedSellerId =
            (autoCompleteTextView.adapter as DokanySimpleAdapter).getSellerId(position)
        }
    }

//    pricesMenu.visibility = View.VISIBLE
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_product_detail, menu)

    val favoriteBtn = menu.findItem(R.id.action_favorite)
    viewModel.isInFavorites().observe(this, Observer {
      setToolbarIconChecked(favoriteBtn, it)
    })

    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId
    if (id == R.id.action_favorite) {
      viewModel.isInFavorites().observe(this, Observer {
        if (!it) {
          viewModel.addToFavorites()
          setToolbarIconChecked(item, true)
          showSnackbar(R.string.added_to_favorites)
        } else {
          viewModel.removeFromFavorites()
          setToolbarIconChecked(item, false)
          showSnackbar(R.string.removed_from_favorites)
        }
      })
    }
    return super.onOptionsItemSelected(item)
  }

  private fun setToolbarIconChecked(menuItem: MenuItem, checked: Boolean) {
    menuItem.isChecked = checked
    menuItem.setIcon(if (!menuItem.isChecked) R.drawable.ic_outline_favorite_24 else R.drawable.ic_baseline_favorite_24)
  }

  private fun showSnackbar(@StringRes stringRes: Int) {
    Snackbar.make(productDetailsSv, stringRes, Snackbar.LENGTH_SHORT).show()
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    contentCl.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }

  override fun onDestroy() {
    super.onDestroy()

    addToCartBtn.setOnClickListener(null)
    removeFromCartBtn.setOnClickListener(null)
    changeQuantityBtn.setOnClickListener(null)
  }

  companion object {
    private val INTENT_PRODUCT_ID = "product_id"

    fun newIntent(context: Context, productId: String): Intent {
      val intent = Intent(context, ProductDetailActivity::class.java)
      intent.putExtra(INTENT_PRODUCT_ID, productId)
      return intent
    }
  }
}