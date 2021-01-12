package com.batache.dokany.app

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.model.adapter.FavoriteModel_
import com.batache.dokany.model.pojo.Product
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.android.synthetic.main.fragment_favorites.view.*

class FavoritesFragment : BaseFragment() {

  private val viewModel: FavoritesViewModel by viewModels {
    FavoritesViewModelFactory(
      (requireActivity().application as DokanyApplication).favoritesRepository
    )
  }

  private lateinit var controller: FavoritesController

  override fun getLayout(): Int = R.layout.fragment_favorites

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    controller = FavoritesController()
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewModel.favoritedProducts.observe(viewLifecycleOwner, Observer { favoritedProducts ->
      controller.setFavoritedProducts(favoritedProducts)
      controller.requestModelBuild()
    })
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (activity as BaseActivity).apply {
      setSupportActionBar(rootView.dokanyTopAppBar.topAppBar)
    }

    rootView.favoritesRv.setController(controller)
  }

  inner class FavoritesController : EpoxyController() {
    private val favoritedProducts: MutableList<Product> = ArrayList()

    fun setFavoritedProducts(products: List<Product>) {
      favoritedProducts.clear()
      favoritedProducts.addAll(products)
    }

    override fun buildModels() {
      for (product in favoritedProducts) {
        FavoriteModel_().apply {
          id(product.id)
          title(product.title)
          image(product.images[0])
          listener(View.OnClickListener {
            startActivity(ProductDetailActivity.newIntent(requireContext(), product.id))
          })
          addTo(this@FavoritesController)
        }
      }
    }
  }
}