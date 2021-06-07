package com.batache.dokany

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun Palette.generateColors(
  // The higher the level, the more white the color has
  @IntRange(from = 0, to = 10) backgroundBlendLevel: Int = 9,
  @IntRange(from = 0, to = 10) darkerBlendLevel: Int = 3
): ProductColors {
  val backgroundColor = blendColors(
    vibrantSwatch?.rgb ?: 0,
    Color.rgb(255, 255, 255),
    backgroundBlendLevel
  )

  val darkerColor = blendColors(
    vibrantSwatch?.rgb ?: 0,
    Color.rgb(255, 255, 255),
    darkerBlendLevel
  )

  return ProductColors(
    backgroundColor,
    darkerColor
  )
}

private fun blendColors(
  @ColorInt firstColor: Int,
  @ColorInt secondColor: Int,
  // The higher the level, the more white the color has
  @IntRange(from = 0, to = 10) blendLevel: Int
): Int {
  val totalStepLevels = 10
  val redStep = (secondColor.red() - firstColor.red()) / totalStepLevels
  val greenStep = (secondColor.green() - firstColor.green()) / totalStepLevels
  val blueStep = (secondColor.blue() - firstColor.blue()) / totalStepLevels

  val r = firstColor.red() + redStep * blendLevel
  val g = firstColor.green() + greenStep * blendLevel
  val b = firstColor.blue() + blueStep * blendLevel

  return Color.rgb(r, g, b)
}

private fun Int.red() = Color.red(this)
private fun Int.green() = Color.green(this)
private fun Int.blue() = Color.blue(this)

fun Int.getHex() = String.format("#%06X", 0xFFFFFF and this)

data class ProductColors(
  @ColorInt val backgroundColor: Int,
  @ColorInt val darkerColor: Int
)

fun GlideRequest<Drawable>.extractColors(
  // The higher the level, the more white the color has
  @IntRange(from = 0, to = 10) backgroundBlendLevel: Int = 9,
  @IntRange(from = 0, to = 10) darkerBlendLevel: Int = 3,
  listener: (ProductColors?) -> Unit
): GlideRequest<Drawable> {
  return listener(object : RequestListener<Drawable> {
    override fun onLoadFailed(
      e: GlideException?,
      model: Any?,
      target: Target<Drawable>?,
      isFirstResource: Boolean
    ): Boolean {
      return true
    }

    override fun onResourceReady(
      resource: Drawable?,
      model: Any?,
      target: Target<Drawable>?,
      dataSource: DataSource?,
      isFirstResource: Boolean
    ): Boolean {
      val bitmap = Bitmap.createScaledBitmap((resource as BitmapDrawable).bitmap, 50.dp, 50.dp, false)
      val builder = Palette.from(bitmap)
      builder.generate { palette ->
        listener.invoke(palette?.generateColors(backgroundBlendLevel, darkerBlendLevel))
      }
      return false
    }
  })
}

fun Int.setAlpha(alpha: Double): Int {
  val alphaColor = Color.alpha(this) * alpha
  val red = Color.red(this)
  val green = Color.green(this)
  val blue = Color.blue(this)

  return Color.argb(alphaColor.toInt(), red, green, blue)
}