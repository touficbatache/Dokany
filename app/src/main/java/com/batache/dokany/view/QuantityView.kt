package com.batache.dokany.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.batache.dokany.R
import kotlinx.android.synthetic.main.layout_quantity_view.view.*

class QuantityView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

  private var onQuantityChangeListener: OnQuantityChangeListener =
    object : OnQuantityChangeListener {
      override fun onChange(oldQuantity: Int, newQuantity: Int) {}
    }

  var min: Int = 1
    set(value) {
      field = value
      quantity = value.coerceAtLeast(quantity)
    }

  var max: Int? = null
    set(value) {
      field = value
      value?.let {
        quantity = it.coerceAtMost(quantity)
      }
    }

  //  private var minString: String? = "1"
//  private var maxString: String? = null

  var quantity: Int
    get() = quantityTv.text.toString().toInt()
    set(value) {
      quantityTv.text = value.toString()
    }

  init {
    inflate(context, R.layout.layout_quantity_view, this)
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    val ta = getContext().obtainStyledAttributes(attrs, R.styleable.QuantityView)
    ta.getString(R.styleable.QuantityView_minQuantity)?.let { minQuantityAttrValue ->
      min = minQuantityAttrValue.toInt()
    }
    max = ta.getString(R.styleable.QuantityView_maxQuantity)?.toIntOrNull()
    ta.recycle()
    quantity = min

    removeBtn.setOnClickListener {
      val currentValue = quantity
      val newValue = min.coerceAtLeast(currentValue - 1)
      quantity = newValue
      onQuantityChangeListener.onChange(currentValue, newValue)
    }
    removeBtn.setOnLongClickListener {
      val currentValue = quantity
      val newValue = min
      quantity = newValue
      onQuantityChangeListener.onChange(currentValue, newValue)
      true
    }
    addBtn.setOnClickListener {
      val currentValue = quantity
      val newValue = if (max != null) {
        max!!.coerceAtMost(currentValue + 1)
      } else {
        currentValue + 1
      }
      quantity = newValue
      onQuantityChangeListener.onChange(currentValue, newValue)
    }
  }

  fun setOnQuantityChangeListener(onQuantityChangeListener: OnQuantityChangeListener) {
    this.onQuantityChangeListener = onQuantityChangeListener
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    if (!enabled) {
      val disabledColor = resources.getColor(R.color.grey_8e)
      removeBtn.isClickable = false
      removeBtn.isLongClickable = false
      removeBtn.setTextColor(disabledColor)

      quantityTv.setTextColor(disabledColor)

      addBtn.isClickable = false
      addBtn.setTextColor(disabledColor)
    } else {
      val colorSecondary = resources.getColor(R.color.grey_8e)
      removeBtn.isClickable = true
      removeBtn.isLongClickable = true
      removeBtn.setTextColor(colorSecondary)

      quantityTv.setTextColor(resources.getColor(R.color.black))

      addBtn.isClickable = true
      addBtn.setTextColor(colorSecondary)
    }
  }

  interface OnQuantityChangeListener {
    fun onChange(oldQuantity: Int, newQuantity: Int)
  }
}