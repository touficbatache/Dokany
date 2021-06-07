package com.batache.dokany.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.appcompat.widget.ListPopupWindow
import com.batache.dokany.DokanySimpleAdapter
import com.batache.dokany.R
import com.batache.dokany.model.pojo.product.ProductSeller
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_seller_price.view.*
import kotlinx.android.synthetic.main.view_dokany_seller_chooser.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DokanySellerChooser @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

  //  private var _sellers: List<ProductSellerDetails> = ArrayList()
  private var _sellers: List<ProductSeller> = ArrayList()
  private var _selectedSellerIndex: Int = 0

  private var listPopupWindow: ListPopupWindow

//  var sellers: List<ProductSellerDetails>
//    get() = _sellers
//    set(value) {
//      _sellers = value
//      invalidateAdapter()
//      invalidateSellers()
//    }

  var sellers: List<ProductSeller>
    get() = _sellers
    set(value) {
      _sellers = value
      invalidateAdapter()
      invalidateSellers()
    }

  var isResetRequired: Boolean = false
  var onReset: (() -> Unit)? = null

  var selectedSellerIndex: Int
    get() = _selectedSellerIndex
    set(value) {
      _selectedSellerIndex = value
      invalidateSellers()
    }

  var selectedSellerId: String?
    get() {
      if (sellers.isNullOrEmpty()) {
        return null
      }
      return sellers[selectedSellerIndex].id
    }
    set(value) {
      if (sellers.isNotEmpty()) {
        selectedSellerIndex = sellers.indexOf(
          sellers.find { it.id == value }
        )
      }
    }

  init {
    inflate(context, R.layout.view_dokany_seller_chooser, this)

    listPopupWindow = ListPopupWindow(context, attrs, R.attr.listPopupWindowStyle).apply {
      isModal = true
      anchorView = card
      setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
        if (isResetRequired) {
          MaterialAlertDialogBuilder(context)
            .setTitle("Product already in cart!")
            .setMessage("Are you sure you want to change the grocery shop for this product?")
            .setNegativeButton("No") { _, _ ->
              dismiss()
            }
            .setPositiveButton("Yes") { _, _ ->
              selectedSellerIndex = position
              invalidateSellers()

              dismiss()

              onReset?.invoke()
            }
            .show()
        } else {
          selectedSellerIndex = position
          invalidateSellers()

          dismiss()
        }
      }
    }

    card.setOnClickListener {
      if (!listPopupWindow.isShowing) {
        listPopupWindow.show()
        listPopupWindow.listView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        listPopupWindow.setSelection(selectedSellerIndex)
      } else {
        listPopupWindow.dismiss()
      }
    }
  }

  private fun invalidateAdapter() {
    val adapterSellers: MutableList<Map<String, String>> = ArrayList()
    sellers.filter { it.price != null }.forEach { adapterSellers.add(generateSeller(it)) }

    val adapter = DokanySimpleAdapter(
      context,
      adapterSellers,
      R.layout.layout_seller_chooser_item,
      arrayOf("price", "seller", "rating", "reviews"),
      intArrayOf(R.id.priceTv, R.id.sellerTv, R.id.ratingBar, R.id.reviewsTv)
    )

    adapter.setViewBinder { view, any, s ->
      if (view is TextView) {
        view.text = s
      } else if (view is RatingBar) {
        view.rating = s.toFloat()
      }
      return@setViewBinder true
    }

    listPopupWindow.setAdapter(adapter)
  }

//  private fun generateSeller(productSeller: ProductSellerDetails): MutableMap<String, String> {
//    val datum: MutableMap<String, String> = HashMap()
//    datum["id"] = productSeller.id
//    datum["price"] = productSeller.price.toString()
//    datum["seller"] = "at ${productSeller.name} \u2022"
//    datum["rating"] = productSeller.starsRating.toString()
//    datum["reviews"] = "(${(113..147).random()} reviews)"
//    return datum
//  }

  private fun generateSeller(productSeller: ProductSeller): MutableMap<String, String> {
    val datum: MutableMap<String, String> = HashMap()
    datum["id"] = productSeller.id
    datum["price"] = productSeller.price.toString()
    datum["seller"] = "at ${productSeller.name} \u2022 ${(460..1600).random()}m away \u2022"
    datum["rating"] = productSeller.rating.toString()
    datum["reviews"] = "(${(113..147).random()} reviews)"
    return datum
  }

  private fun invalidateSellers() {
    sellers[selectedSellerIndex].price?.let { price ->
      priceTv.price = price
    }
    sellerTv.text = "from ${sellers[selectedSellerIndex].name}"
  }
}