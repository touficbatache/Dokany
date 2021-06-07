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
import kotlinx.android.synthetic.main.model_list_item_two_line.view.*

@EpoxyModelClass(layout = R.layout.model_list_item_two_line)
abstract class ListItemTwoLineModel :
  EpoxyModelWithHolder<ListItemTwoLineModel.DokanyTwoLineListItemHolder>() {

  @EpoxyAttribute
  var firstLine: String? = null

  @EpoxyAttribute
  var secondLine: String? = null

  @EpoxyAttribute
  var endImageUrl: Object? = null

  @EpoxyAttribute
  lateinit var onClickListener: View.OnClickListener

  override fun bind(holder: DokanyTwoLineListItemHolder) {
    super.bind(holder)

    holder.lineOneTv.text = firstLine
    holder.lineTwoTv.text = secondLine
    GlideApp
      .with(holder.image.context)
      .load(endImageUrl)
      .into(holder.image)
    holder.itemView.setOnClickListener(onClickListener)
  }

  override fun unbind(holder: DokanyTwoLineListItemHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  class DokanyTwoLineListItemHolder : EpoxyHolder() {
    lateinit var itemView: View
    lateinit var lineOneTv: TextView
    lateinit var lineTwoTv: TextView
    lateinit var image: ImageView

    override fun bindView(itemView: View) {
      this.itemView = itemView
      lineOneTv = itemView.lineOneTv
      lineTwoTv = itemView.lineTwoTv
      image = itemView.image
    }
  }
}