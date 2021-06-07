package com.batache.dokany.model.adapter

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R
import com.batache.dokany.model.pojo.Order
import com.batache.dokany.view.PriceTextView
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.model_order.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@EpoxyModelClass(layout = R.layout.model_order)
abstract class OrderModel : EpoxyModelWithHolder<OrderModel.OrderHolder>() {

  @EpoxyAttribute
  lateinit var order: Order

  @EpoxyAttribute
  lateinit var listener: View.OnClickListener

  override fun bind(holder: OrderHolder) {
    super.bind(holder)

//      StringBuilder title = new StringBuilder();
//      for (int i = 0; i < order.lineItems.size(); i++) {
//        if (i == 0) {
//          title = new StringBuilder(order.lineItems.get(i).name);
//        } else {
//          title.append(" & ").append(order.lineItems.get(i).name);
//        }
//      }
////        holder.tvTitle.setText(title);
//      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
////            tvProductName.setText(categoryList.name + "");
//        holder.tvTitle.setText(Html.fromHtml(title + "", Html.FROM_HTML_MODE_LEGACY));
//      } else {
////            tvProductName.setText(categoryList.name + "");
//        holder.tvTitle.setText(Html.fromHtml(title + ""));
//      }

    order.date?.let {
      holder.dateTv.text = "Placed on: " +
        SimpleDateFormat("E, MMM d, y 'at' h:m a", Locale.getDefault())
          .format(Date(it))
          .toString()
    }

    val itemCountName = if (order.products.size > 1) "items" else "item"
    val sellerIds: MutableList<String> = ArrayList()
    order.products.forEach { product ->
      if (!sellerIds.contains(product.sellerId)) {
        sellerIds.add(product.sellerId)
      }
    }
    holder.itemCountTv.text = "${order.products.size} $itemCountName"

    val storeCountName = if (sellerIds.size > 1) "stores" else "store"
    holder.storeCountTv.text = "${sellerIds.size} $storeCountName"
//    holder.storeCountTv.text = "Ordered ${order.products.size} $itemCountName from ${sellerIds.size} $storeCountName."

    holder.deliveryAddressTv.text = order.deliveryAddress
    holder.deliveryTimeTv.text = order.deliveryTime

    var totalPrice = 0
    order.products.forEach { product ->
      totalPrice += (product.unitPrice ?: 0) * (product.quantity ?: 0) + (order.deliveryPrice ?: 0)
    }
    holder.totalPriceTv.price = totalPrice

    holder.statusTv.text = order.status?.toString()?.toLowerCase()?.capitalize()

//      @StringRes int statusDesc = -1;
//      switch (order.status.toLowerCase()) {
//        case RequestParamsUtils.any:
//        case RequestParamsUtils.shipping:
//          statusDesc = R.string.delivered_soon;
//          break;
//        case RequestParamsUtils.pending:
//          statusDesc = R.string.order_is_in_pending_state;
//          break;
//        case RequestParamsUtils.processing:
//          statusDesc = R.string.order_is_under_processing;
//          break;
//        case RequestParamsUtils.onHold:
//          statusDesc = R.string.order_is_on_hold;
//          break;
//        case RequestParamsUtils.completed:
//          statusDesc = R.string.delivered;
//          break;
//        case RequestParamsUtils.cancelled:
//          statusDesc = R.string.order_is_cancelled;
//          break;
//        case RequestParamsUtils.refunded:
//          statusDesc = R.string.you_are_refunded_for_this_order;
//          break;
//        case RequestParamsUtils.failed:
//          statusDesc = R.string.order_is_failed;
//          break;
//      }
//      holder.statusTv.setText(statusDesc);
    holder.viewOrderBtn.setOnClickListener(listener)
  }

  override fun unbind(holder: OrderHolder) {
    super.unbind(holder)

    holder.viewOrderBtn.setOnClickListener(null)
  }

  class OrderHolder : EpoxyHolder() {
    lateinit var itemView: View
    lateinit var dateTv: TextView
    lateinit var itemCountTv: TextView
    lateinit var storeCountTv: TextView
    lateinit var deliveryAddressTv: TextView
    lateinit var deliveryTimeTv: TextView
    lateinit var totalPriceTv: PriceTextView
    lateinit var statusTv: TextView
    lateinit var viewOrderBtn: MaterialButton

    override fun bindView(itemView: View) {
      this.itemView = itemView
      dateTv = itemView.dateTv
      itemCountTv = itemView.itemCountTv
      storeCountTv = itemView.storeCountTv
      deliveryAddressTv = itemView.deliveryAddressTv
      deliveryTimeTv = itemView.deliveryTimeTv
      totalPriceTv = itemView.totalPriceTv
      statusTv = itemView.statusTv
      viewOrderBtn = itemView.viewOrderBtn
    }
  }
}