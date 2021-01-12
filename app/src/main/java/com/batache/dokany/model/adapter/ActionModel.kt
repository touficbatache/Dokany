package com.batache.dokany.model.adapter

import android.view.View
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R
import com.google.android.material.button.MaterialButton

@EpoxyModelClass(layout = R.layout.layout_action)
abstract class ActionModel : EpoxyModelWithHolder<ActionModel.ActionHolder>() {

  @EpoxyAttribute
  @DrawableRes
  var icon: Int? = null

  @EpoxyAttribute
  lateinit var title: String

  @EpoxyAttribute
  lateinit var onClickListener: View.OnClickListener

  override fun bind(holder: ActionHolder) {
    super.bind(holder)

    icon?.let {
      holder.actionBtn.setIconResource(it)
      holder.actionBtn.text = title
    }
    holder.actionBtn.setOnClickListener(onClickListener)
  }

  override fun unbind(holder: ActionHolder) {
    super.unbind(holder)

    holder.actionBtn.setOnClickListener(null)
  }

  class ActionHolder : EpoxyHolder() {

    lateinit var actionBtn: MaterialButton

    override fun bindView(itemView: View) {
      this.actionBtn = itemView as MaterialButton
    }

  }

}