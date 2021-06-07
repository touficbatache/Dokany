package com.batache.dokany.model.adapter

import android.view.View
import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.*
import com.batache.dokany.R
import com.batache.dokany.model.adapter.base.BaseHolder
import com.batache.dokany.model.pojo.product.Product
import com.batache.dokany.search
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.model_category.view.*

@EpoxyModelClass(layout = R.layout.model_category)
abstract class CategoryModel : EpoxyModelWithHolder<CategoryModel.CategoryHolder>() {

  private val controller: ProductsController by lazy { ProductsController() }

  @EpoxyAttribute
  var title: String? = null

  @EpoxyAttribute
  lateinit var onViewAllClick: (categoryId: String) -> Unit

  @EpoxyAttribute
  lateinit var products: List<Product>

  @EpoxyAttribute
  lateinit var listener: ProductTapListener

  override fun bind(holder: CategoryHolder) {
    super.bind(holder)

    holder.viewAllBtn.text = title
    holder.viewAllBtn.setOnClickListener {
      title?.toLowerCase()?.let { categoryId -> onViewAllClick.invoke(categoryId) }
    }

    holder.productsRv.setController(controller)
//    holder.productsRv.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
//      override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
//        return object : EdgeEffect(recyclerView.context) {
//
//          override fun onPull(deltaDistance: Float) {
//            super.onPull(deltaDistance)
//            handlePull(deltaDistance)
//          }
//
//          override fun onPull(deltaDistance: Float, displacement: Float) {
//            super.onPull(deltaDistance, displacement)
//            handlePull(deltaDistance)
//          }
//
//          private fun handlePull(deltaDistance: Float) {
//            // This is called on every touch event while the list is scrolled with a finger.
//            // We simply update the view properties without animation.
//            val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
//            val rotationDelta = sign * deltaDistance * OVERSCROLL_ROTATION_MAGNITUDE
//            val translationYDelta =
//              sign * recyclerView.width * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
//            recyclerView.forEachVisibleHolder { holder: BaseHolder ->
//              holder.rotation.cancel()
//              holder.translationY.cancel()
//              holder.getItem().rotation += rotationDelta
//              holder.getItem().translationY += translationYDelta
//            }
//          }
//
//          override fun onRelease() {
//            super.onRelease()
//            // The finger is lifted. This is when we should start the animations to bring
//            // the view property values back to their resting states.
//            recyclerView.forEachVisibleHolder { holder: BaseHolder ->
//              holder.rotation.start()
//              holder.translationY.start()
//            }
//          }
//
//          override fun onAbsorb(velocity: Int) {
//            super.onAbsorb(velocity)
//            val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
//            // The list has reached the edge on fling.
//            val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
//            recyclerView.forEachVisibleHolder { holder: BaseHolder ->
//              holder.translationY
//                .setStartVelocity(translationVelocity)
//                .start()
//            }
//          }
//        }
//      }
//    }
    controller.setProducts(products)
    controller.requestModelBuild()
  }

//  companion object {
//    /** The magnitude of rotation while the list is scrolled. */
//    private const val SCROLL_ROTATION_MAGNITUDE = 0.25f
//
//    /** The magnitude of rotation while the list is over-scrolled. */
//    private const val OVERSCROLL_ROTATION_MAGNITUDE = -10
//
//    /** The magnitude of translation distance while the list is over-scrolled. */
//    private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 0.2f
//
//    /** The magnitude of translation distance when the list reaches the edge on fling. */
//    private const val FLING_TRANSLATION_MAGNITUDE = 0.5f
//  }
//
//  inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(
//    action: (T) -> Unit
//  ) {
//    for (i in 0 until childCount) {
//      action(getChildViewHolder(getChildAt(i)) as T)
//    }
//  }

  override fun unbind(holder: CategoryHolder) {
    super.unbind(holder)

    holder.viewAllBtn.setOnClickListener(null)
  }

  class CategoryHolder : EpoxyHolder() {
    lateinit var viewAllBtn: MaterialButton
    lateinit var productsRv: EpoxyRecyclerView

    override fun bindView(itemView: View) {
      viewAllBtn = itemView.viewAllBtn
      productsRv = itemView.productsRv
    }
  }

  inner class ProductsController : EpoxyController() {
    private val products: MutableList<Product> = ArrayList()

    private var query = ""
    private val searchedProducts: MutableList<Product> = ArrayList()

    fun setProducts(products: List<Product>) {
      this.products.clear()
      this.products.addAll(products)
    }

    fun searchProducts(query: String) {
      this.query = query
      searchedProducts.clear()
      searchedProducts.addAll(products.search(query))
    }

    fun clear() {
      products.clear()
    }

    override fun buildModels() {
      val shownProducts =
        if (query.isEmpty() && searchedProducts.isEmpty()) products else searchedProducts

      for (i in IntRange(1, 3)) {
        for (product in shownProducts) {
          product {
            id("${product.id}_$i")
            withSlideLayout()
            product(product)
            listener(listener)
          }
        }
      }
    }
  }
}