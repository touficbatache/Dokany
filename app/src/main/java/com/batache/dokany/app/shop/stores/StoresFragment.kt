package com.batache.dokany.app.shop.stores

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.viewModels
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.model.adapter.seller
import com.batache.dokany.model.pojo.Seller
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_stores.*
import kotlinx.android.synthetic.main.fragment_stores.view.*

class StoresFragment : BaseFragment() {

  private val viewModel: StoresViewModel by viewModels {
    StoresViewModel.Factory(requireActivity().application as DokanyApplication)
  }

  private lateinit var controller: SellersController

  override fun getLayout(): Int = R.layout.fragment_stores

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

//    enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
//    returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.sellers.observe(viewLifecycleOwner, { sellers ->
      controller.setSellers(sellers)
      controller.requestModelBuild()
    })

    controller = SellersController()
    rootView.sellersRv.setController(controller)

    searchingNearbyAvd.applyLoopingAnimatedVectorDrawable(R.drawable.avd_searching_nearby)
  }

  inner class SellersController : EpoxyController() {
    private val sellers: MutableList<Seller> = ArrayList()

    fun setSellers(sellers: List<Seller>) {
      this.sellers.clear()
      this.sellers.addAll(sellers)
    }

    override fun buildModels() {
      for (seller in sellers) {
        seller {
          id(seller.id)
          seller(seller)
          listener(View.OnClickListener {
            startActivity(StoreDetailsActivity.newIntent(requireContext(), seller.id))
          })
        }
      }
    }
  }

  internal fun ImageView.applyLoopingAnimatedVectorDrawable(@DrawableRes avdResId: Int) {
    val animated = AnimatedVectorDrawableCompat.create(context, avdResId)
    animated?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
      override fun onAnimationEnd(drawable: Drawable?) {
        this@applyLoopingAnimatedVectorDrawable.post { animated.start() }
      }
    })
    this.setImageDrawable(animated)
    animated?.start()
  }
}