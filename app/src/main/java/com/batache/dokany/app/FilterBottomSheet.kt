package com.batache.dokany.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.R
import com.batache.dokany.model.adapter.ActionModel_
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheet : BottomSheetDialogFragment() {

  private lateinit var rootView: View
  private lateinit var controller: ActionsController

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    rootView = inflater.inflate(R.layout.bottom_sheet_filter, container, false)

    controller = ActionsController()

    return rootView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

//    controller.apply {
//      rootView.actionsRv.setController(this)
//
//      addAction("coupons", R.drawable.ic_outline_ticket_percent_24, "Coupons")
//
//      requestModelBuild()
//    }
  }

  fun onActionClick(id: String) {
    when (id) {
      "coupons" -> startActivity(Intent(context, CouponsActivity::class.java))
    }

    dismiss()
  }

  inner class ActionsController : EpoxyController() {

    private val actions: MutableList<Action> = ArrayList()

    fun addAction(id: String, @DrawableRes icon: Int, title: String) {
      actions.add(Action(id, icon, title))
    }

    override fun buildModels() {
      for (action in actions) {
        ActionModel_().apply {
          id(action.id)
          icon(action.icon)
          title(action.title)
          onClickListener(View.OnClickListener { onActionClick(action.id) })
          addTo(this@ActionsController)
        }
      }
    }

  }

  data class Action(
    val id: String,
    @DrawableRes val icon: Int,
    val title: String
  )

  companion object {
    const val TAG = "MenuBottomSheet"
  }

}