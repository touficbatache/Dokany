package com.batache.dokany.model.adapter

import android.graphics.Paint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.DoubleClickListener
import com.batache.dokany.GlideApp
import com.batache.dokany.R
import com.batache.dokany.app.ShopFragment
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.layout_product.view.*

@EpoxyModelClass(layout = R.layout.layout_product)
abstract class ProductModel : EpoxyModelWithHolder<ProductModel.ProductHolder>() {

  @EpoxyAttribute
  lateinit var title: String

  @EpoxyAttribute
  var price: Int? = null

  @EpoxyAttribute
  lateinit var relevantDetail: String

  @EpoxyAttribute
  lateinit var images: List<String>

  @EpoxyAttribute
  lateinit var listener: ShopFragment.ProductTapListener

  override fun bind(holder: ProductHolder) {
    super.bind(holder)

    holder.title.text = title
    holder.priceBeforeSale.apply {
      paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }
    holder.price.text = "From $price LBP"
    holder.relevantDetail.text = relevantDetail
    GlideApp.with(holder.image.context)
      .load(FirebaseStorage.getInstance().getReferenceFromUrl(images[0]))
      .into(holder.image)
    holder.itemView.setOnClickListener(object : DoubleClickListener() {
      override fun onClick(v: View?) {
        listener.onClick()
      }

      override fun onDoubleClick() {
        listener.onDoubleTap()
      }
    })
  }

  override fun unbind(holder: ProductHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  override fun getSpanSize(totalSpanCount: Int, position: Int, itemCount: Int): Int {
    return 1
  }

  class ProductHolder : EpoxyHolder() {
    lateinit var itemView: View
    lateinit var title: TextView
    lateinit var priceBeforeSale: TextView
    lateinit var price: TextView
    lateinit var relevantDetail: TextView
    lateinit var image: ImageView

    override fun bindView(itemView: View) {
      this.itemView = itemView
      title = itemView.title
      priceBeforeSale = itemView.priceBeforeSale
      price = itemView.price
      relevantDetail = itemView.relevantDetail
      image = itemView.image
    }
  }
}