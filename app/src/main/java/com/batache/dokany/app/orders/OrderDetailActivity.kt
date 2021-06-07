package com.batache.dokany.app.orders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.app.base.LoadingActivity
import com.batache.dokany.app.cart.CartActivity
import com.batache.dokany.model.pojo.Order
import com.batache.dokany.model.pojo.OrderStatus
import com.batache.dokany.model.pojo.cart.CartProductJoin
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.app_bar.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderDetailActivity : LoadingActivity(), View.OnClickListener {

  private val order by lazy { Gson().fromJson(intent.getStringExtra(KEY_ORDER), Order::class.java) }
  private val orderId by lazy { intent.getStringExtra(KEY_ORDER_ID) }

  private val viewModel: OrderDetailViewModel by viewModels {
    OrderDetailViewModel.Factory(application as DokanyApplication)
  }

  override fun getLayout(): Int = R.layout.activity_order_detail

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    if (order != null) {
      loadOrder(order)
      onDataFetched()
    } else if (orderId != null) {
      viewModel.getOrder(orderId!!)
    }

    viewModel.order.observe(this) { order ->
      order?.let {
        loadOrder(it)
        onDataFetched()
      }
    }
  }

  private fun loadOrder(order: Order) {
    order.date?.let {
      dateTv.text = "Placed on: " +
          SimpleDateFormat("E, MMM d, y 'at' h:m a", Locale.getDefault())
            .format(Date(it))
            .toString()
    }

    val itemCountName = if (order.products.size > 1) "items" else "item"
    val itemCount = "${order.products.size} $itemCountName"
    itemCountTv.text = itemCount

    val sellerIds: MutableList<String> = ArrayList()
    order.products.forEach { product ->
      if (!sellerIds.contains(product.sellerId)) {
        sellerIds.add(product.sellerId)
      }
    }
    val storeCountName = if (sellerIds.size > 1) "stores" else "store"
    val storeCount = "${sellerIds.size} $storeCountName"

    storeCountTv.text = storeCount

    deliveryAddressTv.text = order.deliveryAddress
    deliveryTimeTv.text = order.deliveryTime

    var totalPrice = 0
    order.products.forEach { product ->
      totalPrice += (product.unitPrice ?: 0) * (product.quantity ?: 0)
    }
    totalPrice += order.deliveryPrice ?: 0
    totalPriceTv.price = totalPrice

    statusTv.text = order.status?.toString()?.toLowerCase()?.capitalize()

    subtotalLabelTv.text = "Subtotal ($itemCount):"
    subtotalTv.price = totalPrice - (order.deliveryPrice ?: 0)

    deliveryPriceLabelTv.text = "Delivery ($storeCount):"
    deliveryPriceTv.price = order.deliveryPrice ?: 0

    reorderBtn.isEnabled = order.status == OrderStatus.DELIVERED
    reorderBtn.setOnClickListener {
      MaterialAlertDialogBuilder(this)
        .setTitle("Your cart contains items!")
        .setMessage("Do you want to clear your cart and reorder this order's items?")
        .setNegativeButton("Cancel") { _, _ -> }
        .setPositiveButton("Clear cart & reorder") { _, _ ->
          viewModel.setCartProducts(
            order.products.map { CartProductJoin(it.productId, it.sellerId, it.quantity) },
//        order.deliveryAddress,
//        order.deliveryTime,
//        order.additionalInfo,
//        order.allowSubstitutions ?: false
          )
          authResultLauncher.launch(Intent(this, CartActivity::class.java))
        }
        .show()
    }
  }

  override fun onClick(view: View?) {
    when (view?.id) {
      R.id.continueShoppingBtn -> finish()
    }
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
//    ordersRv.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }

  companion object {
    private const val KEY_ORDER = "KEY_ORDER"
    private const val KEY_ORDER_ID = "KEY_ORDER_ID"

    fun newIntent(context: Context, order: Order): Intent {
      val intent = Intent(context, OrderDetailActivity::class.java)
      intent.putExtra(KEY_ORDER, Gson().toJson(order))
      return intent
    }

    fun newIntent(context: Context, orderId: String): Intent {
      val intent = Intent(context, OrderDetailActivity::class.java)
      intent.putExtra(KEY_ORDER_ID, orderId)
      return intent
    }
  }
}