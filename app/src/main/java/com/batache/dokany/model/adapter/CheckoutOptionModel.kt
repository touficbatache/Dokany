package com.batache.dokany.model.adapter

import android.view.View
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.model_checkout_option.view.*

@EpoxyModelClass(layout = R.layout.model_checkout_option)
abstract class CheckoutOptionModel : EpoxyModelWithHolder<CheckoutOptionModel.CheckoutOptionHolder>() {

  @EpoxyAttribute
  @DrawableRes
  var icon: Int? = null

  @EpoxyAttribute
  lateinit var text: String

  @EpoxyAttribute
  lateinit var onClickListener: View.OnClickListener

  override fun bind(holder: CheckoutOptionHolder) {
    super.bind(holder)

    icon?.let { holder.listItemBtn.setIconResource(it) }
    holder.listItemBtn.text = text
    holder.listItemBtn.setOnClickListener(onClickListener)
  }

  override fun unbind(holder: CheckoutOptionHolder) {
    super.unbind(holder)

    holder.listItemBtn.setOnClickListener(null)
  }

  class CheckoutOptionHolder : EpoxyHolder() {
    lateinit var listItemBtn: MaterialButton

    override fun bindView(itemView: View) {
      listItemBtn = itemView.listItemBtn
    }
  }
}