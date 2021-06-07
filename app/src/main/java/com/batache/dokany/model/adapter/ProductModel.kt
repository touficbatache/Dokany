package com.batache.dokany.model.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.*
import com.batache.dokany.model.adapter.base.BaseHolder
import com.batache.dokany.model.pojo.product.Product
import com.batache.dokany.view.PriceTextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.model_product.view.*

@EpoxyModelClass(layout = R.layout.model_product, useLayoutOverloads = true)
abstract class ProductModel : EpoxyModelWithHolder<ProductModel.ProductHolder>() {

  @EpoxyAttribute
  lateinit var product: Product

  private val showCartIndicator: Boolean by lazy { product.sellers.isNotEmpty() }

  private val inCart: Boolean by lazy { product.isInCart }

  private val initialPrice: Int? by lazy {
    product.sellers.map { seller -> seller.price ?: Int.MAX_VALUE }.minOrNull()
  }

  @EpoxyAttribute
  lateinit var listener: ProductTapListener

  override fun bind(holder: ProductHolder) {
    super.bind(holder)

    holder.titleTv.text = product.title

    holder.priceTv?.price = if (initialPrice != null) initialPrice!! else 0
    holder.priceTv?.visibility = if (initialPrice != null) View.VISIBLE else View.GONE

    holder.relevantDetailTv.text = product.relevantDetail
    GlideApp.with(holder.imageIv.context)
      .load(FirebaseStorage.getInstance().getReferenceFromUrl(product.images[0]))
      .extractColors(backgroundBlendLevel = 9) { colors ->
        colors?.let {
          if (holder.colorSplash != null) {
            holder.colorSplash?.setCardBackgroundColor(colors.backgroundColor)
          } else {
            holder.addToCartBtn?.setBackgroundColor(colors.backgroundColor.setAlpha(0.4))
          }
        }
      }
      .into(holder.imageIv)

    holder.itemView.setOnClickListener(object : DoubleClickListener() {
      override fun onSingleClick(view: View?) {
        listener.onSingleClick(product.id)
      }

      override fun onDoubleClick(view: View?) {
        listener.onDoubleClick(product.id)

        holder.favoriteAnim?.let {
          it.alpha = 0.9F
          val drawable =
            AnimatedVectorDrawableCompat.create(it.context, R.drawable.avd_favorite)
          it.setImageDrawable(drawable)
          drawable?.start()
        }
      }
    })
    holder.addToCartBtn?.apply {
      visibility = if (showCartIndicator) View.VISIBLE else View.GONE
      if (inCart) {
        setBackgroundResource(R.drawable.bg_white_40_percent)
        setImageResource(R.drawable.ic_line_shopping_cart_check)
        setOnClickListener(null)
      } else {
        setBackgroundResource(R.drawable.bg_white_40_percent_ripple)
        setImageResource(R.drawable.ic_line_shopping_cart_add)
        setOnClickListener { listener.onAddToCartClick(product.id, product.sellers.map { it.id }) }
      }
    }
  }

  override fun unbind(holder: ProductHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
    holder.addToCartBtn?.setOnClickListener(null)
  }

  class ProductHolder : BaseHolder() {
    lateinit var itemView: View
    var colorSplash: MaterialCardView? = null
    lateinit var imageIv: ImageView
    lateinit var titleTv: TextView
    lateinit var relevantDetailTv: TextView
    var priceTv: PriceTextView? = null
    var favoriteAnim: ImageView? = null
    var addToCartBtn: ShapeableImageView? = null

    override fun bindView(itemView: View) {
      this.itemView = itemView
      colorSplash = itemView.colorSplash
      imageIv = itemView.imageIv
      titleTv = itemView.titleTv
      relevantDetailTv = itemView.relevantDetailTv
      priceTv = itemView.priceTv
      favoriteAnim = itemView.favoriteAnim
      addToCartBtn = itemView.addToCartBtn
    }

    override fun getItem(): View = itemView
  }
}