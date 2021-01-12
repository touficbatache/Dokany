package com.batache.dokany.model.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageButton
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R
import kotlinx.android.synthetic.main.layout_dokany_top_app_bar_action.view.*

@EpoxyModelClass(layout = R.layout.layout_dokany_top_app_bar_action)
abstract class DokanyTopAppBarActionModel :
  EpoxyModelWithHolder<DokanyTopAppBarActionModel.EndIconHolder>() {

  @EpoxyAttribute
  var icon: Drawable? = null

  @EpoxyAttribute
  lateinit var onClickListener: View.OnClickListener

  override fun bind(holder: EndIconHolder) {
    super.bind(holder)

    holder.itemView.setOnClickListener(onClickListener)
    icon?.let {
      holder.iconIb.setImageDrawable(icon)
    }
  }

  override fun unbind(holder: EndIconHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  class EndIconHolder : EpoxyHolder() {

    lateinit var itemView: View
    lateinit var iconIb: ImageButton

    override fun bindView(itemView: View) {
      this.itemView = itemView
      iconIb = itemView.iconIb
    }

  }

}