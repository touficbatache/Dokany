package com.batache.dokany.app.shop.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.*
import com.batache.dokany.app.account.authenticate.AuthenticateActivity
import com.batache.dokany.app.base.LoadingFragment
import com.batache.dokany.app.shop.products.product_details.ProductDetailsActivity
import com.batache.dokany.model.adapter.*
import com.batache.dokany.model.pojo.product.Product
import com.batache.dokany.view.DokanyTopAppBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_products.view.*

class ProductsFragment : LoadingFragment(), DokanyTopAppBar.OnActionClickListener {

  private val viewModel: ProductsViewModel by viewModels {
    ProductsViewModel.Factory(requireActivity().application as DokanyApplication)
  }

  private var isProductBeingAddedToFavorites = false

  private lateinit var controller: ProductsController
  private var layoutMode = LayoutMode.STAGGERED
    set(value) {
      field = value
      activity?.getPreferences(Context.MODE_PRIVATE)?.edit()
        ?.putString("LAYOUT_MODE", value.toString())?.apply()

      rootView.productsRv.fadeOut(150) {
        updateLayoutPadding()
        controller.requestModelBuild()
      }

      runIn(500) {
        rootView.productsRv.fadeIn(150)
      }
    }
    get() = LayoutMode.valueOf(
      activity?.getPreferences(Context.MODE_PRIVATE)?.getString("LAYOUT_MODE", "STAGGERED")
        ?: "STAGGERED"
    )

  override fun getLayout(): Int = R.layout.fragment_products

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

//    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
//    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootView.twoGridBtn.setOnClickListener {
      layoutMode = LayoutMode.STAGGERED
    }

    rootView.threeGridBtn.setOnClickListener {
      layoutMode = LayoutMode.GRID_COMPACT
    }

    rootView.slideGridBtn.setOnClickListener {
      layoutMode = LayoutMode.SLIDE
    }

    rootView.layoutModeButtons.check(
      when (layoutMode) {
        LayoutMode.STAGGERED -> {
          R.id.twoGridBtn
        }
        LayoutMode.GRID_COMPACT -> {
          R.id.threeGridBtn
        }
        LayoutMode.SLIDE -> {
          R.id.slideGridBtn
        }
      }
    )

    viewModel.products.observe(viewLifecycleOwner, { products ->
      if (isProductBeingAddedToFavorites) {
        return@observe
      }
      updateLayoutPadding()
      controller.setProducts(products)
      controller.requestModelBuild()
      onLoaded()
    })

    viewModel.eventAuthError.observe(viewLifecycleOwner, { isAuthError ->
      if (isAuthError) onAuthError()
    })

    controller = ProductsController()
    rootView.productsRv.setController(controller)
    rootView.productsRv.enforceSingleScrollDirection()
  }

  private fun updateLayoutPadding() {
    when (layoutMode) {
      LayoutMode.STAGGERED -> {
        rootView.productsRv.updatePadding(left = 8.dp, right = 8.dp)
        rootView.productsRv.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
      }
      LayoutMode.GRID_COMPACT -> {
        rootView.productsRv.updatePadding(left = 0, right = 0)
        rootView.productsRv.layoutManager = GridLayoutManager(context, 3)
      }
      LayoutMode.SLIDE -> {
        rootView.productsRv.updatePadding(left = 0, right = 0)
        rootView.productsRv.layoutManager =
          LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      }
    }
  }

  override fun onTopAppBarActionClick(id: Int) {
//    when (id) {
//      R.id.action_search -> startActivity(Intent(context, SearchActivity::class.java))
//    }
  }

  private fun onAuthError() {
    showSnackbar(R.string.auth_error, R.string.sign_in) {
      startActivity(Intent(requireContext(), AuthenticateActivity::class.java))
    }
  }

  private fun showSnackbar(
    @StringRes message: Int,
    @StringRes actionText: Int = -1,
    actionClickListener: View.OnClickListener? = null
  ) {
    Snackbar
      .make(rootView, message, Snackbar.LENGTH_SHORT)
      .setAnchorView(mainActivity?.favoritesFab)
      .setAction(actionText, actionClickListener)
      .show()
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

      if (layoutMode == LayoutMode.STAGGERED || layoutMode == LayoutMode.GRID_COMPACT) {

        promotion {
          id("promotion")
        }

        for (product in shownProducts) {
          product {
            id(product.id)
            if (layoutMode == LayoutMode.GRID_COMPACT) withCompactLayout()
            product(product)
            listener(object : ProductTapListener {
              override fun onSingleClick(productId: String) {
                authResultLauncher.launch(
                  ProductDetailsActivity.newIntent(requireContext(), productId)
                )
              }

              override fun onDoubleClick(productId: String) {
                isProductBeingAddedToFavorites = true
                viewModel.addToFavorites(productId)
                runIn(800) {
                  isProductBeingAddedToFavorites = false
                }
              }

              override fun onAddToCartClick(productId: String, productSellerIds: List<String>) {
                QuickAddToCartDialog.newInstance(
                  productId,
                  ArrayList(productSellerIds)
                ).show(requireActivity().supportFragmentManager, QuickAddToCartDialog.TAG)
              }
            })
          }
        }

      } else {

//        promotion {
//          id("promotion")
//          spanSize(6)
//        }

        val categories = shownProducts.map { it.category }.distinct()

        for (category in categories) {
          category {
            id(category)
            title(category.capitalize())
            onViewAllClick { categoryId ->
              authResultLauncher.launch(
                CategoryActivity.newIntent(requireContext(), categoryId)
              )
            }
            products(shownProducts.filter { it.category == category })
            listener(object : ProductTapListener {
              override fun onSingleClick(productId: String) {
                authResultLauncher.launch(
                  ProductDetailsActivity.newIntent(requireContext(), productId)
                )
              }

              override fun onDoubleClick(productId: String) {
                isProductBeingAddedToFavorites = true
                viewModel.addToFavorites(productId)
                runIn(800) {
                  isProductBeingAddedToFavorites = false
                }
              }

              override fun onAddToCartClick(productId: String, productSellerIds: List<String>) {
                QuickAddToCartDialog.newInstance(
                  productId,
                  ArrayList(productSellerIds)
                ).show(requireActivity().supportFragmentManager, QuickAddToCartDialog.TAG)
              }
            })
          }
        }

      }
    }
  }

  private enum class LayoutMode {
    STAGGERED, GRID_COMPACT, SLIDE
  }
}