package com.batache.dokany.model.adapter

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.batache.dokany.R
import com.batache.dokany.model.pojo.FamilyBondList

@EpoxyModelClass(layout = R.layout.model_shopping_list)
abstract class ShoppingListModel : EpoxyModelWithHolder<ShoppingListModel.ShoppingListHolder>() {

  @EpoxyAttribute
  lateinit var shoppingList: FamilyBondList

  @EpoxyAttribute
  lateinit var listener: View.OnClickListener

  override fun bind(holder: ShoppingListHolder) {
    super.bind(holder)

  }

  override fun unbind(holder: ShoppingListHolder) {
    super.unbind(holder)

    holder.itemView.setOnClickListener(null)
  }

  class ShoppingListHolder : EpoxyHolder() {
    lateinit var itemView: View

    override fun bindView(itemView: View) {
      this.itemView = itemView
    }
  }
}