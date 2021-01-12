package com.batache.dokany.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.model.adapter.ActionModel_
import com.batache.dokany.util.StringUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.view.*

class AccountFragment : BaseFragment() {

  private val TAG = "AccountFragment : "

  private val authActivityRequestCode = 2884

  private lateinit var controller: ActionsController

  lateinit var auth: FirebaseAuth

  override fun getLayout(): Int = R.layout.fragment_account

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    controller = ActionsController()

    auth = FirebaseAuth.getInstance()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootView.contentCl.visibility = View.GONE

    if (auth.currentUser == null) {
      startActivityForResult(
        Intent(requireContext(), AuthenticateActivity::class.java),
        authActivityRequestCode
      )
      return
    }

    initAccountPage()
  }

  private fun initAccountPage() {
    rootView.phoneNumberTv.text = auth.currentUser?.phoneNumber?.let {
      StringUtils.formatLebanesePhoneNumber(it)
    }

    controller.apply {
      rootView.actionsRv.setController(this)

//      val popupMenu = PopupMenu(context, null)
//      popupMenu.inflate(R.menu.menu_main)
//      popupMenu.menu.iterator()

      addAction("family_accounts", R.drawable.ic_baseline_family_restroom_24, "Family accounts")
      addAction("coupons", R.drawable.ic_outline_ticket_percent_24, "Coupons")
      addAction("orders", R.drawable.ic_outline_receipt_long_24, "Orders")

      requestModelBuild()
    }

    rootView.contentCl.visibility = View.VISIBLE

    rootView.signOutBtn.setOnClickListener {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle("Sign out?")
        .setMessage("You won't be able to apply coupons or see your previous orders.")
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Sign out") { _, _ ->
          auth.signOut()
          goBack()
        }
        .show()
    }
  }

  fun onActionClick(id: String) {
    when (id) {
      "coupons" -> startActivity(Intent(context, CouponsActivity::class.java))
      "orders" -> startActivity(Intent(context, OrdersActivity::class.java))
    }
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == authActivityRequestCode) {
      if (auth.currentUser != null) {
        initAccountPage()
      } else {
        goBack()
      }
    }
  }

  private fun goBack() {
    (requireActivity() as MainActivity).goBack()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    rootView.signOutBtn.setOnClickListener(null)
  }

}