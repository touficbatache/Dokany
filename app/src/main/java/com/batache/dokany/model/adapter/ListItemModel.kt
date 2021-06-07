package com.batache.dokany.model.adapter

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.model_list_item.view.*

@EpoxyModelClass(layout = R.layout.model_list_item)
abstract class ListItemModel : EpoxyModelWithHolder<ListItemModel.ListItemHolder>() {

  @EpoxyAttribute
  var icon: Drawable? = null

  @EpoxyAttribute
  @DrawableRes
  var iconRes: Int? = null

  @EpoxyAttribute
  var title: String? = null

  @EpoxyAttribute
  lateinit var onClickListener: View.OnClickListener

  override fun bind(holder: ListItemHolder) {
    super.bind(holder)

    holder.listItemBtn.setOnClickListener(onClickListener)
    icon?.let {
      holder.listItemBtn.icon = it
    }
    iconRes?.let {
      holder.listItemBtn.setIconResource(it)
    }
    holder.listItemBtn.text = title
  }

  override fun unbind(holder: ListItemHolder) {
    super.unbind(holder)

    holder.listItemBtn.setOnClickListener(null)
  }

  class ListItemHolder : EpoxyHolder() {

    lateinit var listItemBtn: MaterialButton

    override fun bindView(itemView: View) {
      listItemBtn = itemView.listItemBtn
    }
  }
}