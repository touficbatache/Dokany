package com.batache.dokany.app.shop.products

import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.batache.dokany.*
import com.batache.dokany.app.shop.products.product_details.ProductDetailsViewModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_quick_add_to_cart.view.*
import kotlin.math.max

class QuickAddToCartDialog : DialogFragment() {

  private lateinit var rootView: View

  private val viewModel: ProductDetailsViewModel by viewModels {
    ProductDetailsViewModel.Factory(
      requireActivity().application as DokanyApplication,
      arguments?.getString(KEY_PRODUCT_ID) ?: ""
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    rootView = inflater.inflate(R.layout.fragment_quick_add_to_cart, container, false)
    return rootView
  }

  override fun onStart() {
    super.onStart()

    dialog?.window?.setLayout(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT
    )

    dialog?.window?.setDimAmount(0.3F)

    dialog?.window?.setElevation(44F)

    applyColors(
      ProductColors(
        backgroundColor = ResourcesCompat.getColor(
          resources,
          android.R.color.white,
          context?.theme
        ),
        darkerColor = ResourcesCompat.getColor(resources, android.R.color.black, context?.theme)
      )
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.product.observe(viewLifecycleOwner) {
      GlideApp.with(requireContext())
        .load(FirebaseStorage.getInstance().getReferenceFromUrl(it.images[0]))
        .extractColors(darkerBlendLevel = 1) { colors ->
          applyColors(colors)
        }
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .preload()
    }

    rootView.closeBtn.setOnClickListener {
      dismiss()
    }

    rootView.quantityPlusBtn.setOnClickListener {
      rootView.quantityCountTv.text =
        (rootView.quantityCountTv.text.toString().toInt() + 1).toString()
    }

    rootView.quantityMinusBtn.setOnClickListener {
      rootView.quantityCountTv.text =
        (max(
          1,
          rootView.quantityCountTv.text.toString().toInt() - 1
        )).toString()
    }

    rootView.addOrGoToCartBtn.setOnClickListener {
      viewModel.addToCart(
        arguments?.getStringArrayList(KEY_PRODUCT_SELLER_IDS)?.get(0),
        rootView.quantityCountTv.text.toString().toInt()
      )
      dismiss()
    }
  }

  private fun applyColors(colors: ProductColors?) {
    colors?.let {
      val shapeAppearanceModel = ShapeAppearanceModel.builder()
        .setAllCornerSizes { 20.dp.toFloat() }
        .build()
      val backgroundDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
        setTint(colors.backgroundColor)
        paintStyle = Paint.Style.FILL
      }
      dialog?.window?.setBackgroundDrawable(backgroundDrawable)

      rootView.quantityPlusBtn.strokeColor = ColorStateList.valueOf(colors.darkerColor)
      rootView.quantityPlusBtn.imageTintList = ColorStateList.valueOf(colors.darkerColor)
      rootView.quantityCountTv.setTextColor(colors.darkerColor)
      rootView.quantityMinusBtn.strokeColor = ColorStateList.valueOf(colors.darkerColor)
      rootView.quantityMinusBtn.imageTintList = ColorStateList.valueOf(colors.darkerColor)
    }
  }

  companion object {
    const val TAG = "QuickAddToCartDialog"

    private const val KEY_PRODUCT_ID = "KEY_PRODUCT_ID"
    private const val KEY_PRODUCT_SELLER_IDS = "KEY_PRODUCT_SELLER_IDS"

    fun newInstance(productId: String, productSellerIds: ArrayList<String>): QuickAddToCartDialog {
      return QuickAddToCartDialog().apply {
        arguments = Bundle().apply {
          putString(KEY_PRODUCT_ID, productId)
          putStringArrayList(KEY_PRODUCT_SELLER_IDS, productSellerIds)
        }
      }
    }
  }
}