package com.batache.dokany.model.adapter

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R

@EpoxyModelClass(layout = R.layout.model_divider)
abstract class DividerModel : EpoxyModelWithHolder<DividerModel.DividerHolder>() {

  class DividerHolder : EpoxyHolder() {
    override fun bindView(itemView: View) {}
  }
}