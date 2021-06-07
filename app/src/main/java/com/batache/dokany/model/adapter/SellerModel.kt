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
import com.batache.dokany.model.pojo.Seller
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.model_seller.view.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar

@EpoxyModelClass(layout = R.layout.model_seller)
abstract class SellerModel : EpoxyModelWithHolder<SellerModel.ProductHolder>() {

  @EpoxyAttribute
  lateinit var seller: Seller

  @EpoxyAttribute
  lateinit var listener: View.OnClickListener

  override fun bind(holder: ProductHolder) {
    super.bind(holder)

    if (seller.profilePicture.isNotBlank()) {
      GlideApp
        .with(holder.profilePicIv.context)
        .load(FirebaseStorage.getInstance().getReferenceFromUrl(seller.profilePicture))
        .into(holder.profilePicIv)
    }
    holder.nameTv.text = seller.name
    if (seller.productCount != null) {
      val productCountName = if (seller.productCount!! > 1) "products" else "product"
      holder.productCountTv.text = "${seller.productCount} $productCountName"
    }

    if (seller.rating?.toFloat() != null) {
      holder.ratingBar.rating = seller.rating?.toFloat()!!
    } else {
      holder.ratingBar.visibility = View.GONE
    }
//    GlideApp.with(holder.image.context)
//      .load(FirebaseStorage.getInstance().getReferenceFromUrl(images[0]))
//      .into(holder.image)
    holder.itemView.setOnClickListener(listener)
  }

  override fun unbind(holder: ProductHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  class ProductHolder : EpoxyHolder() {
    lateinit var itemView: View
    lateinit var profilePicIv: ImageView
    lateinit var nameTv: TextView
    lateinit var productCountTv: TextView
    lateinit var ratingBar: MaterialRatingBar

    override fun bindView(itemView: View) {
      this.itemView = itemView
      profilePicIv = itemView.profilePicIv
      nameTv = itemView.nameTv
      productCountTv = itemView.productCountTv
      ratingBar = itemView.ratingBar
    }
  }
}