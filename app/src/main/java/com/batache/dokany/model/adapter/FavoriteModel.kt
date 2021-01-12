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
import kotlinx.android.synthetic.main.layout_product.view.*

@EpoxyModelClass(layout = R.layout.layout_favorite)
abstract class FavoriteModel : EpoxyModelWithHolder<FavoriteModel.FavoriteHolder>() {

  @EpoxyAttribute
  lateinit var image: String

  @EpoxyAttribute
  lateinit var title: String

  @EpoxyAttribute
  lateinit var listener: View.OnClickListener

  override fun bind(holder: FavoriteHolder) {
    super.bind(holder)

    GlideApp.with(holder.image.context)
      .load(FirebaseStorage.getInstance().getReferenceFromUrl(image))
      .into(holder.image)
    holder.title.text = title
    holder.itemView.setOnClickListener(listener)
  }

  override fun unbind(holder: FavoriteHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  class FavoriteHolder : EpoxyHolder() {
    lateinit var itemView: View
    lateinit var image: ImageView
    lateinit var title: TextView

    override fun bindView(itemView: View) {
      this.itemView = itemView
      image = itemView.image
      title = itemView.title
    }
  }
}