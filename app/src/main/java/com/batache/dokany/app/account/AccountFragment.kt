package com.batache.dokany.app.account

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.batache.dokany.R
import com.batache.dokany.app.account.addresses.AddressesActivity
import com.batache.dokany.app.account.authenticate.AuthenticateActivity
import com.batache.dokany.app.account.coupons.CouponsActivity
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.formatPhoneNumberLBN
import com.batache.dokany.view.DokanyListView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.view.*

class AccountFragment : BaseFragment(), DokanyListView.OnItemClickListener {

  private lateinit var auth: FirebaseAuth

  private var authenticateResultLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      if (auth.currentUser != null) {
        initAccountPage()
      } else {
        goBack()
      }
    }

  override fun getLayout(): Int = R.layout.fragment_account

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    auth = FirebaseAuth.getInstance()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    rootView.contentCl.visibility = View.GONE

    if (auth.currentUser == null) {
      authenticateResultLauncher.launch(Intent(requireContext(), AuthenticateActivity::class.java))
      return
    }

    initAccountPage()
  }

  private fun initAccountPage() {
    rootView.signedInNameOrPhoneTv.text =
      if (auth.currentUser?.displayName.isNullOrBlank()) {
        auth.currentUser?.phoneNumber?.formatPhoneNumberLBN()
      } else {
        auth.currentUser?.displayName!!
      }

    rootView.cardHolderNameTv.text = "Mr. ${auth.currentUser?.displayName}"

    rootView.listView.setActionClickListener(this)

    rootView.contentCl.visibility = View.VISIBLE
  }

  override fun onItemClick(id: Int) {
    when (id) {
      R.id.addresses -> startActivity(Intent(context, AddressesActivity::class.java))
      R.id.coupons -> startActivity(Intent(context, CouponsActivity::class.java))
      R.id.orders -> findNavController().navigate(R.id.ordersFragment)
      R.id.sign_out -> {
        MaterialAlertDialogBuilder(requireContext())
          .setTitle("Sign out?")
          .setMessage("You'll be disconnected from Family Bond, won't be able to apply coupons or see your previous orders.")
          .setNegativeButton("Cancel", null)
          .setPositiveButton("Sign out") { _, _ ->
            auth.signOut()
            goBack()
          }
          .show()
      }
    }
  }

  private fun goBack() {
    mainActivity?.goBack()
  }
}