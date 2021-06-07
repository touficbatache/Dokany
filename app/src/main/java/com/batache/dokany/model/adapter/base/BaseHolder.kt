package com.batache.dokany.model.adapter.base

import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.airbnb.epoxy.EpoxyHolder

abstract class BaseHolder : EpoxyHolder() {
  abstract fun getItem(): View

  var currentVelocity = 0f

  /**
   * A [SpringAnimation] for this RecyclerView item. This animation rotates the view with a bouncy
   * spring configuration, resulting in the oscillation effect.
   *
   * The animation is started in [Recyclerview.onScrollListener].
   */
  val rotation: SpringAnimation by lazy {
    SpringAnimation(getItem(), SpringAnimation.ROTATION)
      .setSpring(
        SpringForce()
          .setFinalPosition(0f)
          .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
          .setStiffness(SpringForce.STIFFNESS_LOW)
      )
      .addUpdateListener { _, _, velocity ->
        currentVelocity = velocity
      }
  }

  /**
   * A [SpringAnimation] for this RecyclerView item. This animation is used to bring the item back
   * after the over-scroll effect.
   */
  val translationY: SpringAnimation by lazy {
    SpringAnimation(getItem(), SpringAnimation.TRANSLATION_Y)
      .setSpring(
        SpringForce()
          .setFinalPosition(0f)
          .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
          .setStiffness(SpringForce.STIFFNESS_LOW)
      )
  }
}