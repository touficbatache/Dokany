package com.batache.dokany.model.adapter

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R

@EpoxyModelClass(layout = R.layout.model_promotion)
abstract class PromotionModel : EpoxyModelWithHolder<PromotionModel.PromotionHolder>() {

  class PromotionHolder : EpoxyHolder() {
    override fun bindView(itemView: View) {}
  }
}