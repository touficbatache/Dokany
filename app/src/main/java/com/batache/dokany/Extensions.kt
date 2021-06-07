package com.batache.dokany

import android.animation.Animator
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import androidx.annotation.Px
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.batache.dokany.model.pojo.Address
import com.batache.dokany.model.pojo.cart.CartProduct
import com.batache.dokany.model.pojo.product.Product
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

fun View.updateMargin(
  @Px left: Int? = null,
  @Px top: Int? = null,
  @Px right: Int? = null,
  @Px bottom: Int? = null
) {
  layoutParams<ViewGroup.MarginLayoutParams> {
    left?.run { leftMargin = this } //dpToPx(this)
    top?.run { topMargin = this } //dpToPx(this)
    right?.run { rightMargin = this } //dpToPx(this)
    bottom?.run { bottomMargin = this } //dpToPx(this)
  }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
  if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int =
  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

val Int.px: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Fragment?.runOnUiThread(action: () -> Unit) {
  this ?: return
  if (!isAdded) return // Fragment not attached to an Activity
  activity?.runOnUiThread(action)
}

//public fun <T> Iterable<T>.search(sourceString: (T) -> String, query: String): List<T> {
//  return filter {
//    var keywordValidated: Boolean? = null
//    query.toLowerCase().split(" ").forEach { keyword ->
//      if (sourceString.toLowerCase().contains(keyword)) {
//        if (keywordValidated == null) keywordValidated = true
//      } else {
//        keywordValidated = false
//      }
//    }
//    keywordValidated ?: false
//  }
//}

fun List<Product>.search(query: String): List<Product> {
  return filter {
    var keywordValidated: Boolean? = null
    query.toLowerCase().split(" ").forEach { keyword ->
      if (it.title.toLowerCase().contains(keyword)) {
        if (keywordValidated == null) keywordValidated = true
      } else {
        keywordValidated = false
      }
    }
    keywordValidated ?: false
  }
}

fun List<CartProduct>.calculateSubtotal(): Int {
  var subtotal = 0
  for (product in this) {
    product.quantity?.let {
      product.productSellers?.find { it.id == product.sellerId }?.price?.let {
        subtotal += product.quantity.times(it)
      }
    }
  }
  return subtotal
}

fun List<Address>.format(): List<String> {
  return map { address ->
    "Floor ${address.floorApartment}, ${address.building} Bldg, ${address.street} St, ${address.area}"
  }
}

fun runIn(delay: Long, r: Runnable) {
  Handler(Looper.getMainLooper()).postDelayed(r, delay)
}

/**
 * RecyclerView better scrolling.
 */
fun RecyclerView.enforceSingleScrollDirection() {
  val enforcer = SingleScrollDirectionEnforcer()
  addOnItemTouchListener(enforcer)
  addOnScrollListener(enforcer)
}

private class SingleScrollDirectionEnforcer : RecyclerView.OnScrollListener(), OnItemTouchListener {

  private var scrollState = RecyclerView.SCROLL_STATE_IDLE
  private var scrollPointerId = -1
  private var initialTouchX = 0
  private var initialTouchY = 0
  private var dx = 0
  private var dy = 0

  override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
    when (e.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        scrollPointerId = e.getPointerId(0)
        initialTouchX = (e.x + 0.5f).toInt()
        initialTouchY = (e.y + 0.5f).toInt()
      }
      MotionEvent.ACTION_POINTER_DOWN -> {
        val actionIndex = e.actionIndex
        scrollPointerId = e.getPointerId(actionIndex)
        initialTouchX = (e.getX(actionIndex) + 0.5f).toInt()
        initialTouchY = (e.getY(actionIndex) + 0.5f).toInt()
      }
      MotionEvent.ACTION_MOVE -> {
        val index = e.findPointerIndex(scrollPointerId)
        if (index >= 0 && scrollState != RecyclerView.SCROLL_STATE_DRAGGING) {
          val x = (e.getX(index) + 0.5f).toInt()
          val y = (e.getY(index) + 0.5f).toInt()
          dx = x - initialTouchX
          dy = y - initialTouchY
        }
      }
    }
    return false
  }

  override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

  override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

  override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
    val oldState = scrollState
    scrollState = newState
    if (oldState == RecyclerView.SCROLL_STATE_IDLE && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
      recyclerView.layoutManager?.let { layoutManager ->
        val canScrollHorizontally = layoutManager.canScrollHorizontally()
        val canScrollVertically = layoutManager.canScrollVertically()
        if (canScrollHorizontally != canScrollVertically) {
          if ((canScrollHorizontally && abs(dy) > abs(dx))
            || (canScrollVertically && abs(dx) > abs(dy))
          ) {
            recyclerView.stopScroll()
          }
        }
      }
    }
  }
}

/**
 * View fading in / out.
 */
inline fun ViewPropertyAnimator.doOnAnimationEnd(
  crossinline action: (animator: Animator?) -> Unit
): Animator.AnimatorListener = addAnimationEndListener(onAnimationEnd = action)

inline fun ViewPropertyAnimator.addAnimationEndListener(
  crossinline onAnimationStart: (animator: Animator?) -> Unit = { _ -> },
  crossinline onAnimationEnd: (animator: Animator?) -> Unit = { _ -> },
  crossinline onAnimationCancel: (animator: Animator?) -> Unit = { _ -> },
  crossinline onAnimationRepeat: (animator: Animator?) -> Unit = { _ -> }
): Animator.AnimatorListener {
  val animatorListener = object : Animator.AnimatorListener {
    override fun onAnimationStart(animator: Animator?) {
      onAnimationStart.invoke(animator)
    }

    override fun onAnimationEnd(animator: Animator?) {
      onAnimationEnd.invoke(animator)
    }

    override fun onAnimationCancel(animator: Animator?) {
      onAnimationCancel.invoke(animator)
    }

    override fun onAnimationRepeat(animator: Animator?) {
      onAnimationRepeat.invoke(animator)
    }
  }
  setListener(animatorListener)

  return animatorListener
}

fun View.fadeIn(durationMs: Long, onAnimationEnd: (animator: Animator?) -> Unit = { _ -> }) {
  animate().alpha(1f).setDuration(durationMs).doOnAnimationEnd(onAnimationEnd)
}

fun View.fadeOut(durationMs: Long, onAnimationEnd: (animator: Animator?) -> Unit = { _ -> }) {
  animate().alpha(0f).setDuration(durationMs).doOnAnimationEnd(onAnimationEnd)
}

fun View.showWithFade(durationMs: Long, onAnimationEnd: (animator: Animator?) -> Unit = { _ -> }) {
  visibility = View.VISIBLE
  runIn(350) {
    animate()
      .alpha(1f)
      .setDuration(durationMs)
      .doOnAnimationEnd(onAnimationEnd)
  }
}

fun View.hideWithFade(durationMs: Long, onAnimationEnd: (animator: Animator?) -> Unit = { _ -> }) {
  animate()
    .alpha(0f)
    .setDuration(durationMs)
    .doOnAnimationEnd {
      visibility = View.GONE
      onAnimationEnd.invoke(it)
    }
}

fun String.formatPhoneNumberLBN(): String = PhoneNumberUtils.formatNumber(this, "LBN")

fun Int.formatPriceLBP(): String = NumberFormat.getNumberInstance(Locale.US).format(this)