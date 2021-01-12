package com.batache.dokany.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.model.adapter.ProductModel_
import com.batache.dokany.model.pojo.Product
import com.batache.dokany.view.DokanyTopAppBar
import kotlinx.android.synthetic.main.fragment_shop.view.*

class ShopFragment : BaseFragment(), DokanyTopAppBar.OnActionClickListener {

  private val viewModel: ShopViewModel by viewModels {
    ShopViewModelFactory(
      (requireActivity().application as DokanyApplication).productsRepository
    )
  }

  private lateinit var controller: ProductsController

  override fun getLayout(): Int = R.layout.fragment_shop

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewModel.products.observe(viewLifecycleOwner, Observer { products ->
      controller.clear()
      for (product in products) {
        controller.addProduct(product)
      }
      controller.requestModelBuild()
    })
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootView.dokanyTopAppBar.setActionClickListener(this)

    controller = ProductsController()
    rootView.productsRv.setController(controller)
  }

  override fun onTopAppBarActionClick(id: Int) {
    when (id) {
      R.id.action_search -> startActivity(Intent(context, SearchActivity::class.java))
    }
  }

  inner class ProductsController : EpoxyController() {
    private val products: MutableList<Product> = ArrayList()

    fun addProduct(product: Product) {
      this.products.add(product)
    }

    fun clear() {
      this.products.clear()
    }

    override fun buildModels() {
//      for (product in products) {
//        ProductModel_().apply {
//          id(product.id)
//          title(product.title)
//          price(product.stores.values.iterator().next().price)
//          relevantDetail(product.relevantDetail)
//          images(product.images)
//          addTo(this@ProductsController)
//        }
//      }

      if (products.isNullOrEmpty()) {
        return
      }

      for (i in IntRange(1, 10)) {
        val product = products[0]
        ProductModel_().apply {
          id(i)
          title(product.title)
          price(350)
          relevantDetail(product.relevantDetail)
          images(product.images)
          listener(object : ProductTapListener {
            override fun onClick() {
              startActivity(ProductDetailActivity.newIntent(requireContext(), product.id))
            }

            override fun onDoubleTap() {
              Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
            }

          })
          addTo(this@ProductsController)
        }
      }
    }

  }

  interface ProductTapListener {
    fun onClick()
    fun onDoubleTap()
  }
}