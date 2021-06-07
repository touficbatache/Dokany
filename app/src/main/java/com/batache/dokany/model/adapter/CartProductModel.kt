package com.batache.dokany.model.adapter

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.GlideApp
import com.batache.dokany.R
import com.batache.dokany.extractColors
import com.batache.dokany.view.PriceTextView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.model_cart_product.view.*

@EpoxyModelClass(layout = R.layout.model_cart_product)
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
      .extractColors { colors ->
        colors?.apply {
          holder.productColorSplash.setCardBackgroundColor(colors.backgroundColor)
          holder.productColorSplash.outlineAmbientShadowColor = Color.YELLOW
          holder.productColorSplash.outlineSpotShadowColor = Color.YELLOW
        }
      }
      .into(holder.imageIv)
    holder.titleTv.text = title
    holder.relevantDetailTv.text = relevantDetail

    unitPrice?.let { unitPrice ->
      holder.unitPriceTv.price = unitPrice
      quantity?.let { quantity ->
        holder.finalPriceTv.price = unitPrice.times(quantity)
      }
    }

    holder.itemView.setOnClickListener(listener)
  }

  override fun unbind(holder: CartProductHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  class CartProductHolder : EpoxyHolder() {
    lateinit var itemView: View
    lateinit var productColorSplash: MaterialCardView
    lateinit var quantityTv: TextView
    lateinit var imageIv: ImageView
    lateinit var titleTv: TextView
    lateinit var relevantDetailTv: TextView
    lateinit var unitPriceTv: PriceTextView
    lateinit var finalPriceTv: PriceTextView

    override fun bindView(itemView: View) {
      this.itemView = itemView
      productColorSplash = itemView.colorSplash
      quantityTv = itemView.quantityTv
      imageIv = itemView.imageIv
      titleTv = itemView.nameTv
      relevantDetailTv = itemView.relevantDetailTv
      unitPriceTv = itemView.unitPriceTv
      finalPriceTv = itemView.finalPriceTv
    }
  }
}