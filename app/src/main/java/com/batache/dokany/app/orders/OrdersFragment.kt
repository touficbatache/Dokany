package com.batache.dokany.app.orders

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.LoadingFragment
import com.batache.dokany.model.adapter.order
import com.batache.dokany.model.pojo.Order
import com.batache.dokany.util.DimensionUtils
import kotlinx.android.synthetic.main.fragment_orders.view.*

class OrdersFragment : LoadingFragment(), View.OnClickListener {

  private val viewModel: OrdersViewModel by viewModels {
    OrdersViewModel.Factory(activity?.application as DokanyApplication)
  }

  private val controller: OrdersController by lazy { OrdersController() }

  override fun getLayout(): Int = R.layout.fragment_orders

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootView.continueShoppingBtn.setOnClickListener(this)

    rootView.ordersRv.setController(controller)

    viewModel.orders.observe(viewLifecycleOwner) {
      controller.setOrders(it)
      controller.requestModelBuild()

      onDataFetched()
    }
  }

  override fun onFetchData() {
    super.onFetchData()

    viewModel.getOrders()
  }

  override fun onDataFetched() {
    super.onDataFetched()

    viewModel.orders.value?.let {
      rootView.emptyOrdersLl.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
    }
  }

  override fun onClick(view: View?) {
    when (view?.id) {
      R.id.continueShoppingBtn -> findNavController().navigate(R.id.shopFragment)
    }
  }

  inner class OrdersController : EpoxyController() {
    private val orders: MutableList<Order> = ArrayList()

    fun setOrders(orders: List<Order>) {
      this.orders.clear()
      this.orders.addAll(orders)
      this.orders.sortByDescending { it.date }
    }

    override fun buildModels() {
      for (order in orders) {
        order {
          id(order.id)
          order(order)
          listener { model, parentView, clickedView, position ->
            startActivity(OrderDetailActivity.newIntent(requireContext(), order))
          }
        }
      }
    }

  }

  override fun onApplyAllWindowInsets() {
    rootView.ordersRv.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }
}