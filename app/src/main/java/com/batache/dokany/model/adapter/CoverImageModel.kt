package com.batache.dokany.model.adapter

import android.view.View
import android.widget.ImageView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.GlideApp
import com.batache.dokany.R

@EpoxyModelClass(layout = R.layout.model_cover_image)
abstract class CoverImageModel : EpoxyModelWithHolder<CoverImageModel.CoverImageHolder>() {

  @EpoxyAttribute
  lateinit var imageUrl: String

  override fun bind(holder: CoverImageHolder) {
    super.bind(holder)

    holder.imageView?.let { imageView ->
      GlideApp.with(imageView.context)
        .load(imageUrl) //FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        .into(imageView)
    }
  }

  class CoverImageHolder : EpoxyHolder() {
    var imageView: ImageView? = null

    override fun bindView(itemView: View) {
      if (itemView is ImageView) {
        imageView = itemView
      }
    }
  }
}