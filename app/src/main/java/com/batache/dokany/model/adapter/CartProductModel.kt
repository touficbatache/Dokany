package com.batache.dokany.model.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.GlideApp
import com.batache.dokany.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.layout_product_cart.view.*

@EpoxyModelClass(layout = R.layout.layout_product_cart)
abstract class CartProductModel : EpoxyModelWithHolder<CartProductModel.CartProductHolder>() {

  @EpoxyAttribute
  var quantity: Int? = null

  @EpoxyAttribute
  lateinit var images: List<String>

  @EpoxyAttribute
  lateinit var title: String

  @EpoxyAttribute
  lateinit var relevantDetail: String

  @EpoxyAttribute
  var unitPrice: Int? = null

  @EpoxyAttribute
  lateinit var listener: View.OnClickListener

  override fun bind(holder: CartProductHolder) {
    super.bind(holder)

    holder.quantityTv.text = "$quantity"
    GlideApp.with(holder.imageIv.context)
      .load(FirebaseStorage.getInstance().getReferenceFromUrl(images[0]))
      .into(holder.imageIv)
    holder.titleTv.text = title
    holder.relevantDetailTv.text = relevantDetail
    holder.unitPriceTv.text = "$unitPrice LBP"
    quantity?.let {
      holder.finalPriceTv.text = "${unitPrice?.times(it)} LBP"
    }

    holder.itemView.setOnClickListener(listener)
  }

  override fun unbind(holder: CartProductHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  class CartProductHolder : EpoxyHolder() {
    lateinit var itemView: View
    lateinit var quantityTv: TextView
    lateinit var imageIv: ImageView
    lateinit var titleTv: TextView
    lateinit var relevantDetailTv: TextView
    lateinit var unitPriceTv: TextView
    lateinit var finalPriceTv: TextView

    override fun bindView(itemView: View) {
      this.itemView = itemView
      quantityTv = itemView.quantityTv
      imageIv = itemView.imageIv
      titleTv = itemView.titleTv
      relevantDetailTv = itemView.relevantDetailTv
      unitPriceTv = itemView.unitPriceTv
      finalPriceTv = itemView.finalPriceTv
    }
  }
}