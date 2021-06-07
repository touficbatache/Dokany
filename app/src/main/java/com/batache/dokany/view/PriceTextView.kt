package com.batache.dokany.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.batache.dokany.R
import com.batache.dokany.formatPriceLBP

class PriceTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

  var price: Int
    get() = text.toString().filter { it.isDigit() }.toInt()
    set(value) {
      text = value.toString()
    }

  var isInitialPrice: Boolean = false
    set(value) {
      field = value
      text = text
    }

  init {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    context.obtainStyledAttributes(attrs, R.styleable.PriceTextView).apply {
      isInitialPrice = getBoolean(R.styleable.PriceTextView_isInitialPrice, false)
      recycle()
    }
  }

  override fun setText(text: CharSequence?, type: BufferType?) {
    if (text.isNullOrEmpty() || !text.matches(".*\\d.*".toRegex())) {
      super.setText(text, type)
      return
    }

    val prefix = if (isInitialPrice) "From " else ""

    super.setText(
      "$prefix${text.toString().filter { it.isDigit() }.toInt().formatPriceLBP()} LBP",
      type
    )
  }


}