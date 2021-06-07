package com.batache.dokany.app.shop

import android.os.Bundle
import android.view.View
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.app.shop.brands.BrandsFragment
import com.batache.dokany.app.shop.products.ProductsFragment
import com.batache.dokany.app.shop.stores.StoresFragment
import kotlinx.android.synthetic.main.fragment_shop.view.*

class ShopFragment : BaseFragment() {

  override fun getLayout(): Int = R.layout.fragment_shop

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootView.segmentedControlView.setListener(::onActionSelected)
  }

  private fun onActionSelected(actionId: Int) {
    val fragment = when (actionId) {
      R.id.action_products -> ProductsFragment()
      R.id.action_stores -> StoresFragment()
      R.id.action_brands -> BrandsFragment()
      else -> null
    }
    fragment?.let {
      childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, it).commit()
    }
  }

}