package com.batache.dokany.app.account.addresses

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.permissionmanager.Permission
import com.batache.dokany.permissionmanager.PermissionManager
import com.batache.dokany.updateMargin
import com.batache.dokany.util.DimensionUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.activity_map_location.*
import kotlinx.android.synthetic.main.activity_map_location.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MapLocationFragment : BaseFragment(), OnMapReadyCallback {

  private val permissionManager = PermissionManager.from(this)

  private lateinit var mMap: GoogleMap
  private var address: Address? = null
    set(value) {
      field = value
      titleTv.text = value?.thoroughfare
      subtitleTv.text = String.format("%s, %s", value?.subAdminArea, value?.featureName)
    }

  override fun getLayout(): Int = R.layout.activity_map_location

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    backBtn.setOnClickListener {
      activity?.onBackPressed()
    }

    val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    mapFragment.getMapAsync(this)

    rootView.continueBtn.setOnClickListener {
      val latLng = mMap.cameraPosition.target
      (activity as AddAddressActivity).onContinueClick(
        latLng.latitude.toString(),
        latLng.longitude.toString(),
        address?.featureName,
        address?.thoroughfare
      )
    }
  }

  override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_silver))
    mMap.uiSettings.isMyLocationButtonEnabled = false

    permissionManager
      .request(Permission.Location)
      .rationale("Dokany needs to access your Location to set the delivery address accurately")
      .checkPermission { granted: Boolean ->
        if (granted) {
          mMap.isMyLocationEnabled = true
          LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener {
            mMap.moveCamera(
              CameraUpdateFactory.newLatLngZoom(
                LatLng(
                  it.latitude,
                  it.longitude
                ), 18f
              )
            )
          }
        } else {
          MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location permission needed")
            .setMessage("Dokany needs to access your Location to set the delivery address accurately")
//            .setPositiveButton("OK",
//              { dialogInterface, i -> //Prompt the user once explanation has been shown
//                ActivityCompat.requestPermissions(
//                  this@MapLocationActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                  MY_PERMISSIONS_REQUEST_LOCATION
//                )
//              })
            .show()
        }
      }

    mMap.setOnCameraIdleListener {
      val latLng = mMap.cameraPosition.target
      lifecycleScope.launch {
        withContext(Dispatchers.IO) {
          val addresses = Geocoder(context, Locale("en"))
            .getFromLocation(latLng.latitude, latLng.longitude, 10)
          withContext(Dispatchers.Main) {
            address = getAddress(addresses)
          }
        }
      }
    }
  }

  private fun getAddress(addresses: List<Address>): Address? {
    if (addresses.isNullOrEmpty()) {
      return null
    }

    val address = addresses[0]
    for (next in addresses) {
      if (address.adminArea.isNullOrEmpty()) {
        address.adminArea = next.adminArea
      }
      if (address.featureName.isNullOrEmpty()) {
        address.featureName = next.featureName
      } else if (address.featureName.equals(address.thoroughfare)) {
        address.featureName = next.featureName
      }
      if (address.locality.isNullOrEmpty()) {
        address.locality = next.locality
      }
      if (address.countryName.isNullOrEmpty()) {
        address.countryName = next.countryName
      }
      if (address.countryCode.isNullOrEmpty()) {
        address.countryCode = next.countryCode
      }
      if (address.subAdminArea.isNullOrEmpty()) {
        address.subAdminArea = next.subAdminArea
      }
      if (address.subLocality.isNullOrEmpty()) {
        address.subLocality = next.subLocality
      }
      if (address.thoroughfare.isNullOrEmpty()) {
        address.thoroughfare = next.thoroughfare
      }
      if (address.subThoroughfare.isNullOrEmpty()) {
        address.subThoroughfare = next.subThoroughfare
      }
    }
    return address
  }

  override fun onApplyAllWindowInsets() {
    rootView.backBtn.updateMargin(top = DimensionUtils.TOP_INSET)
    rootView.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }
}