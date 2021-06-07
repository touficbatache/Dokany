package com.batache.dokany.app.cart.checkout

import android.animation.LayoutTransition
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.updatePadding
import androidx.transition.TransitionManager
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.account.addresses.AddAddressActivity
import com.batache.dokany.app.base.LoadingActivity
import com.batache.dokany.calculateSubtotal
import com.batache.dokany.format
import com.batache.dokany.model.adapter.checkoutOption
import com.batache.dokany.util.Constants
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.activity_checkout.contentCl
import kotlinx.android.synthetic.main.app_bar.*

class CheckoutActivity : LoadingActivity() {

  private val viewModel: CheckoutViewModel by viewModels {
    CheckoutViewModel.Factory(application as DokanyApplication)
  }

  private val deliveryAddressController: CheckoutOptionChooserController by lazy { CheckoutOptionChooserController() }
  private val deliveryTimeController: CheckoutOptionChooserController by lazy { CheckoutOptionChooserController() }

  private var deliveryAddress: String? = null
  private var deliveryTime: String? = null

  private var isInDeliveryInfoEditMode: Boolean = true
    set(value) {
      field = value
      deliveryInformation.visibility = if (value) View.GONE else View.VISIBLE
      deliveryInfoEditIv.visibility = if (value) View.GONE else View.VISIBLE
      if (!value) {
        editModeOverlay.visibility = View.GONE
      }
    }

  private val areSubstitutionsAllowed get() = substitutionsVs.currentView == substitutionsCheckedIv

  override fun getLayout(): Int = R.layout.activity_checkout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    contentCl.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

    deliveryAddressChooserRv.setController(deliveryAddressController)

    deliveryTimeController.let {
      deliveryTimeChooserRv.setController(it)
      it.setOptions(
        arrayListOf(
          "Now",
          "In 30 minutes",
          "In 45 minutes",
          "In 1 hour"
        ),
        ::onDeliveryTimeChosen
      )
      it.requestModelBuild()
    }

    deliveryInfoEditIv.setOnClickListener {
      editDeliveryInformation()
    }

    substitutionsVs.setOnClickListener {
      substitutionsVs.showNext()
    }

    viewModel.checkoutInformationState.observe(this) {
      /**
       * List user addresses.
       */
      deliveryAddressController.let { controller ->
        controller.setOptions(it.addresses.format(), ::onDeliveryAddressChosen)
        controller.addCustomOption(R.drawable.ic_line_plus, "Add new address") { option ->
          startActivity(Intent(this@CheckoutActivity, AddAddressActivity::class.java))
        }
        controller.requestModelBuild()
        onDataFetched()
      }

      /**
       * Load subtotal and calculate total price.
       */
      subtotalLabelTv.text = "Subtotal (${it.cartProducts.size} items)"
      subtotalTv.price = it.cartProducts.calculateSubtotal()

      totalPriceTv.price = subtotalTv.price + deliveryTv.price

      /**
       * Enable / Disable "Place order" button, based on the user's choices.
       */
      placeOrderBtn.isEnabled = it.isDeliveryAddressChosen && it.isDeliveryTimeChosen

      /**
       * If order has been placed, go back and clear cart.
       */
      it.isOrderPlaced?.let { isOrderPlaced ->
        if (isOrderPlaced) {
          onOrderPlaced()
        } else {
          showSnackbar(R.string.error_already_ongoing_order_exists, R.string.view_order) {
            val intent = Intent()
            setResult(Constants.GO_HOME, intent)
            finish()
          }
        }
      }
      }

    placeOrderBtn.setOnClickListener {
      if (deliveryAddress != null && deliveryTime != null) {
        enableLoading()

        viewModel.placeOrder(
          deliveryAddress!!,
          deliveryTime!!,
          additionalInstructionsEt.text.toString(),
          areSubstitutionsAllowed
        )
      }
    }
  }

  override fun onFetchData() {
    super.onFetchData()

    viewModel.getCurrentUserAddresses()
    viewModel.getCartProducts()
  }

  private fun editDeliveryInformation() {
    MaterialFadeThrough().let {
      TransitionManager.beginDelayedTransition(deliveryInfoContainer, it)

      isInDeliveryInfoEditMode = true
      deliveryAddressChooser.visibility = View.VISIBLE
    }
  }

  private fun onDeliveryAddressChosen(address: String) {
    deliveryAddress = address
    chosenDeliveryAddressTv.text = address

    MaterialFadeThrough().let {
      TransitionManager.beginDelayedTransition(deliveryInfoContainer, it)

      deliveryAddressChooser.visibility = View.GONE
      deliveryTimeChooser.visibility = View.VISIBLE
    }

    viewModel.setDeliveryAddressChosen(true)
  }

  private fun onDeliveryTimeChosen(time: String) {
    deliveryTime = time
    chosenDeliveryTimeTv.text = time

    MaterialFadeThrough().let {
      TransitionManager.beginDelayedTransition(deliveryInfoContainer, it)

      isInDeliveryInfoEditMode = false
      deliveryTimeChooser.visibility = View.GONE
    }

    viewModel.setDeliveryTimeChosen(true)
  }

  private fun onOrderPlaced() {
    viewModel.onOrderPlaced()
    val intent = Intent()
    setResult(Constants.GO_HOME, intent)
    finish()
  }

  inner class CheckoutOptionChooserController : EpoxyController() {
    private val options: MutableList<String> = ArrayList()
    private var optionListener: ((option: String) -> Unit)? = null

    @DrawableRes
    private var customOptionIcon: Int? = null
    private var customOptionText: String? = null
    private var customOptionListener: ((option: String) -> Unit)? = null

    fun setOptions(options: List<String>, optionListener: (option: String) -> Unit) {
      this.options.clear()
      this.options.addAll(options)
      this.optionListener = optionListener
    }

    fun addOption(option: String) {
      options.add(option)
    }

    fun addCustomOption(
      @DrawableRes icon: Int,
      text: String,
      optionListener: (option: String) -> Unit
    ) {
      customOptionIcon = icon
      customOptionText = text
      customOptionListener = optionListener
    }

    override fun buildModels() {
      for ((i, option) in options.withIndex()) {
        checkoutOption {
          id("option_$i")
          text(option)
          onClickListener { model, parentView, clickedView, position ->
            optionListener?.invoke(options[position])
          }
        }
      }
      if (customOptionIcon != null && customOptionText != null) {
        checkoutOption {
          id("custom_option")
          icon(customOptionIcon)
          text(customOptionText!!)
          onClickListener { model, parentView, clickedView, position ->
            customOptionListener?.invoke("custom_option")
          }
        }
      }
    }
  }

  private fun showSnackbar(
    @StringRes message: Int,
    @StringRes actionText: Int = -1,
    actionClickListener: View.OnClickListener? = null
  ) {
    Snackbar
      .make(rootView, message, Snackbar.LENGTH_LONG)
      .setAction(actionText, actionClickListener)
      .show()
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    contentCl.updatePadding(
      bottom =
      if (DimensionUtils.KEYBOARD_INSET == 0)
        DimensionUtils.BOTTOM_INSET
      else
        DimensionUtils.KEYBOARD_INSET
    )
  }
}