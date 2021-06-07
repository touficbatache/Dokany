package com.batache.dokany.view

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import androidx.core.view.iterator
import com.batache.dokany.R
import com.batache.dokany.dp
import com.google.android.material.animation.AnimationUtils
import kotlinx.android.synthetic.main.view_segmented_control.view.*

class SegmentedControlView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

  private var containerWidth: Int? = null
  private val buttons: MutableList<TextView> = ArrayList()
  private val buttonIds: MutableList<Int> = ArrayList()
  private val buttonDividers: MutableList<View> = ArrayList()
  private var selectedActionIndex: Int = 0
  private var listener: ((id: Int) -> Unit)? = null

  init {
    inflate(context, R.layout.view_segmented_control, this)

    context.obtainStyledAttributes(attrs, R.styleable.DokanyTopAppBar).apply {
      val actionsRes = getResourceId(R.styleable.DokanyTopAppBar_actions, 0)
      setActions(actionsRes)
      recycle()
    }
  }

  private fun setActions(@MenuRes actionsRes: Int) {
    val menu: Menu = PopupMenu(context, null).menu
    MenuInflater(context).inflate(actionsRes, menu)

    buttons.clear()

    val iterate = menu.iterator()
    while (iterate.hasNext()) {
      iterate.next().let { menuItem ->
        TextView(context).let {
          buttons.add(it)
          buttonIds.add(menuItem.itemId)
          it.layoutParams = LinearLayout.LayoutParams(
            0,
            LayoutParams.MATCH_PARENT,
            1.0f
          )
          it.gravity = Gravity.CENTER
          it.setTextColor(resources.getColor(R.color.black))
          it.textSize = 13f
          it.text = menuItem.title
          it.setOnClickListener { clickedBtn ->
            selectedActionIndex = buttons.indexOf(clickedBtn)
            invalidateView()
          }
          actionsContainer.addView(it)
        }

        if (iterate.hasNext()) {
          View(context).let {
            buttonDividers.add(it)
            it.layoutParams = LinearLayout.LayoutParams(
              1.dp,
              15.dp
            ).apply {
              gravity = Gravity.CENTER_VERTICAL
            }
            it.setBackgroundResource(R.color.segmented_control_divider)
            it.alpha = 0.3f
            actionsContainer.addView(it)
          }
        }

        return@let
      }
    }

    container.doOnLayout {
      containerWidth = container.measuredWidth
      invalidateSelector()
    }

    invalidateView()
  }

  private fun invalidateSelector() {
    containerWidth?.let {
      selector.layoutParams = selector.layoutParams.apply { width = it / buttons.size - 4.dp }
    }
  }

  private fun invalidateView() {
    buttons.forEachIndexed { index, button ->
      val font = if (index == selectedActionIndex) R.font.averta_semibold else R.font.averta_regular
      button.typeface = ResourcesCompat.getFont(context, font)
    }

    containerWidth?.let {
      selector
        .animate()
        .translationX(selectedActionIndex * (it / buttons.size).toFloat())
        .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
        .setDuration(400)
    }

    buttonDividers.forEachIndexed { index, buttonDivider ->
      val alpha = if (index == selectedActionIndex - 1 || index == selectedActionIndex) 0f else 0.3f
      buttonDivider
        .animate()
        .alpha(alpha)
        .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
        .setDuration(200)
    }

    listener?.invoke(buttonIds[selectedActionIndex])
  }

  fun setListener(listener: (actionId: Int) -> Unit) {
    this.listener = listener
    invalidateView()
  }
}