package com.batache.dokany.app.account.addresses

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.LoadingActivity
import com.batache.dokany.model.adapter.listItemTwoLine
import com.batache.dokany.model.adapter.listItem
import com.batache.dokany.model.pojo.Address
import com.batache.dokany.util.DimensionUtils
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_addresses.*
import kotlinx.android.synthetic.main.app_bar.*

class AddressesActivity : LoadingActivity() {

  private val viewModel: AddressesViewModel by viewModels {
    AddressesViewModel.Factory(application as DokanyApplication)
  }

  private val controller: AddressesController by lazy { AddressesController() }

  override fun getLayout(): Int = R.layout.activity_addresses

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    addressesRv.setController(controller)

    viewModel.userAddresses.observe(this) {
      controller.setAddresses(it)
      controller.requestModelBuild()

      onDataFetched()
    }
  }

  override fun onFetchData() {
    super.onFetchData()

    viewModel.getCurrentUserAddresses()
  }

  inner class AddressesController : EpoxyController() {
    private val addresses: MutableList<Address> = ArrayList()

    fun setAddresses(addresses: List<Address>) {
      this.addresses.clear()
      this.addresses.addAll(addresses)
    }

    override fun buildModels() {
      listItem {
        id("add_address")
        icon(getDrawable(R.drawable.ic_line_plus))
        title("Add a new address")
        onClickListener(View.OnClickListener {
          startActivity(Intent(this@AddressesActivity, AddAddressActivity::class.java))
        })
      }
      for ((i, address) in addresses.withIndex()) {
        listItemTwoLine {
          id("address_$i")
          firstLine(address.title)
          secondLine("Floor ${address.floorApartment}, ${address.building} Bldg, ${address.street} St, ${address.area}")
          endImageUrl(FirebaseStorage.getInstance().getReference("buildingImages/${address.image}"))
          onClickListener { model, parentView, clickedView, position ->

          }
        }
      }
    }
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    addressesRv.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }
}