package com.batache.dokany.app.shop.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.app.shop.products.product_details.ProductDetailsActivity
import com.batache.dokany.model.adapter.*
import com.batache.dokany.model.pojo.product.Product
import com.batache.dokany.search
import com.batache.dokany.util.DimensionUtils
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_shop.view.*

class CategoryActivity : BaseActivity() {

  private val categoryId: String? by lazy { intent.getStringExtra(KEY_CATEGORY_ID) }

  private val viewModel: ProductsViewModel by viewModels {
    ProductsViewModel.Factory(application as DokanyApplication)
  }

  private val controller: ProductsController by lazy { ProductsController() }

  override fun getLayout(): Int = R.layout.activity_category

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = categoryId?.capitalize()

    productsRv.setController(controller)

    viewModel.products.observe(this) {
      controller.setProducts(it.filter { product -> product.category == categoryId })
      controller.requestModelBuild()
    }

    searchEt.doOnTextChanged { query, start, before, count ->
      controller.searchProducts(query.toString())
      controller.requestModelBuild()
    }
  }

  inner class ProductsController : EpoxyController() {
    private val products: MutableList<Product> = ArrayList()

    private var query = ""
    private val searchedProducts: MutableList<Product> = ArrayList()

    fun setProducts(products: List<Product>) {
      this.products.clear()
      this.products.addAll(products)
    }

    fun searchProducts(query: String) {
      this.query = query
      searchedProducts.clear()
      searchedProducts.addAll(products.search(query))
    }

    fun clear() {
      products.clear()
    }

    override fun buildModels() {
      val shownProducts =
        if (query.isEmpty() && searchedProducts.isEmpty()) products else searchedProducts

      for (product in shownProducts) {
        product {
          id(product.id)
          withCompactLayout()
          product(product)
          listener(object : ProductTapListener {
            override fun onSingleClick(productId: String) {
              authResultLauncher.launch(
                ProductDetailsActivity.newIntent(this@CategoryActivity, productId)
              )
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

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
  }

  companion object {
    private const val KEY_CATEGORY_ID = "KEY_CATEGORY_TITLE"

    fun newIntent(context: Context, productId: String): Intent {
      val intent = Intent(context, CategoryActivity::class.java)
      intent.putExtra(KEY_CATEGORY_ID, productId)
      return intent
    }
  }
}