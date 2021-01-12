package com.batache.dokany.view

import android.content.Context
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.ListPopupWindow
import com.batache.dokany.DokanySimpleAdapter
import com.batache.dokany.R
import com.batache.dokany.model.pojo.ProductSellerDetails
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.layout_dokany_seller_chooser.view.*
import kotlinx.android.synthetic.main.layout_seller_price.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class DokanySellerChooser @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : MaterialCardView(context, attrs, defStyle) {

  private var _sellers: List<ProductSellerDetails> = ArrayList()
  private var _selectedSellerIndex: Int = 0

  private var listPopupWindow: ListPopupWindow
  private var isOpen: Boolean = false
  private lateinit var textPaint: TextPaint
  private var textWidth: Float = 0f
  private var textHeight: Float = 0f
//  private var controller: ActionIconsController? = null

  var sellers: List<ProductSellerDetails>
    get() = _sellers
    set(value) {
      _sellers = value
      invalidateAdapter()
      invalidateSellers()
    }

  var selectedSellerIndex: Int
    get() = _selectedSellerIndex
    set(value) {
      _selectedSellerIndex = value
      invalidateSellers()
    }

  init {
    inflate(context, R.layout.layout_dokany_seller_chooser, this)

    listPopupWindow = ListPopupWindow(context, attrs, R.attr.listPopupWindowStyle)
    listPopupWindow.anchorView = card

    listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
      selectedSellerIndex = position
      invalidateSellers()

      listPopupWindow.dismiss()
    }

    listPopupWindow.setOnDismissListener {
      Timer().schedule(100) {
        isOpen = false
      }
    }

    card.setOnClickListener {
      if (!isOpen) {
        listPopupWindow.show()
        isOpen = true
      } else {
        listPopupWindow.dismiss()
      }
//      isOpen = !isOpen
    }
  }

  private fun invalidateAdapter() {
    val adapterSellers: MutableList<Map<String, String>> = ArrayList()

    for (productSeller in sellers) {
      if (productSeller.price != null) {
        val datum: MutableMap<String, String> = HashMap()
        datum["id"] = productSeller.id
        datum["price"] = "${productSeller.price} LBP"
        datum["seller_rating"] =
          "at ${productSeller.name} \u2022 ${productSeller.starsRating} stars (${(113..147).random()} reviews)"
        adapterSellers.add(datum)
      }
    }

    val adapter = DokanySimpleAdapter(
      context,
      adapterSellers,
      R.layout.layout_seller_price,
      arrayOf("price", "seller_rating"),
      intArrayOf(R.id.priceTv, R.id.sellerAndRatingTv)
    )

    listPopupWindow.setAdapter(adapter)
  }

  private fun invalidateSellers() {
    priceTv.text = "${sellers[selectedSellerIndex].price} LBP"
    sellerAndRatingTv.text = "from ${sellers[selectedSellerIndex].name}"
  }

//  inner class ActionIconsController : EpoxyController() {
//    override fun buildModels() {
//      for (seller in sellers) {
//        DokanyTopAppBarActionModel_().apply {
//          id(actionIcon.itemId)
//          icon(actionIcon.icon)
//          onClickListener(OnClickListener { actionClickListener?.onTopAppBarActionClick(actionIcon.itemId) })
//          addTo(this@ActionIconsController)
//        }
//      }
//    }
//
//  }
}