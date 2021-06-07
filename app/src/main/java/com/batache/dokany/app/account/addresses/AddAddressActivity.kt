package com.batache.dokany.app.account.addresses

import android.os.Bundle
import androidx.activity.viewModels
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.model.pojo.Address
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_address.*

class AddAddressActivity : BaseActivity() {

  private val viewModel: AddressesViewModel by viewModels {
    AddressesViewModel.Factory(application as DokanyApplication)
  }

  override fun getLayout(): Int = R.layout.activity_add_address

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragmentContainer, MapLocationFragment())
      .commit()

    viewModel.eventFirestoreError.observe(this) {
      if (it) {
        Snackbar.make(
          fragmentContainer,
          "Couldn't save your address. Try again later.",
          Snackbar.LENGTH_LONG
        ).show()
      }
    }
    viewModel.eventFirestoreSuccess.observe(this) {
      if (it) {
        Snackbar.make(
          fragmentContainer,
          "Address saved successfully!",
          Snackbar.LENGTH_SHORT
        ).show()
      }
    }
  }

  fun onContinueClick(latitude: String, longitude: String, area: String?, street: String?) {
    supportFragmentManager
      .beginTransaction()
      .replace(
        R.id.fragmentContainer,
        EditAddressFragment.newInstance(latitude, longitude, area, street)
      )
      .addToBackStack(null)
      .commit()
  }

  fun onSaveAddressClick(address: Address) {
    viewModel.addAddress(address)
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount > 0) {
      supportFragmentManager.popBackStack()
      return
    }

    super.onBackPressed();
  }

  override fun onApplyAllWindowInsets() {

  }
}