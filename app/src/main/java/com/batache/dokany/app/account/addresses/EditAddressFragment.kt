package com.batache.dokany.app.account.addresses

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseFragment
import com.batache.dokany.app.base.FirebaseStorageEvent
import com.batache.dokany.model.pojo.Address
import com.batache.dokany.runIn
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.android.synthetic.main.fragment_edit_address.view.*

class EditAddressFragment : BaseFragment() {

  private val viewModel: AddressesViewModel by viewModels {
    AddressesViewModel.Factory(requireActivity().application as DokanyApplication)
  }

  private val latitude by lazy { arguments?.getString(LATITUDE_KEY) ?: "" }
  private val longitude by lazy { arguments?.getString(LONGITUDE_KEY) ?: "" }
  private val area by lazy { arguments?.getString(AREA_KEY) }
  private val street by lazy { arguments?.getString(STREET_KEY) }

  private var isFormValid = false

  var buildingImageUri: Uri? = null
  private var isBuildingImageAttached = false
    set(isAttached) {
      field = isAttached

      rootView.attachBuildingImageBtn.isEnabled = !isAttached

      val bitmap = if (isAttached)
        MediaStore.Images.Media.getBitmap(context?.contentResolver, buildingImageUri) else null
      rootView.attachedImageIv.setImageBitmap(bitmap)

      rootView.attachedBuildingImageStatus.visibility =
        if (isAttached) View.VISIBLE else View.GONE
      rootView.statusAttachedContent.visibility =
        if (isAttached) View.VISIBLE else View.GONE
    }
  private val pickImage =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
      uri?.let { it ->
        buildingImageUri = it
        isBuildingImageAttached = true
      }
    }

  override fun getLayout(): Int = R.layout.fragment_edit_address

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true)
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    activity?.apply {
      setSupportActionBar(rootView.topAppBar)
      supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    rootView.areaEt.setText(area)
    rootView.streetEt.setText(street)

    rootView.attachBuildingImageBtn.setOnClickListener {
      selectImage()
    }

    viewModel.eventFirebaseStorage.observe(viewLifecycleOwner) {
      rootView.statusAttachedContent.visibility =
        if (it is FirebaseStorageEvent.Failure)
          View.VISIBLE
        else
          View.GONE

      when (it) {
        is FirebaseStorageEvent.Progress -> {
          rootView.statusUploadingContent.visibility = View.VISIBLE
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rootView.imageUploadProgress.setProgress(it.progress, true)
          } else {
            rootView.imageUploadProgress.progress = it.progress
          }
          rootView.statusUploadedContent.visibility = View.GONE

          rootView.saveBtn.isEnabled = false
        }
        is FirebaseStorageEvent.Success -> {
          rootView.statusUploadingContent.visibility = View.GONE
          rootView.imageUploadProgress.progress = 0
          rootView.statusUploadedContent.visibility = View.VISIBLE

          saveAddressInFirestore(it.imageTitle)
        }
        FirebaseStorageEvent.Failure -> {
          rootView.statusUploadingContent.visibility = View.GONE
          rootView.imageUploadProgress.progress = 0
          rootView.statusUploadedContent.visibility = View.GONE

          rootView.saveBtn.isEnabled = true
        }
      }
    }

    rootView.removeAttachmentBtn.setOnClickListener {
      buildingImageUri = null
      isBuildingImageAttached = false
    }

    rootView.saveBtn.setOnClickListener {
      validateForm()
      if (isFormValid) {
        if (buildingImageUri != null) {
          viewModel.uploadPhoto(buildingImageUri!!)
        } else {
          saveAddressInFirestore()
        }
      }
    }
  }

  private fun saveAddressInFirestore(imageTitle: String = "") {
    (activity as AddAddressActivity).onSaveAddressClick(
      Address(
        rootView.addressTitleEt.text.toString(),
        latitude,
        longitude,
        rootView.areaEt.text.toString(),
        rootView.streetEt.text.toString(),
        rootView.buildingEt.text.toString(),
        rootView.floorApartmentEt.text.toString(),
        rootView.additionalInstructionsEt.text.toString(),
        imageTitle
      )
    )

    runIn(1000) {
      activity?.finish()
    }
  }

  private fun validateForm() {
    val addressTitleValid = rootView.addressTitleEt.validateRequired(rootView.addressTitleIl)
    val areaValid = rootView.areaEt.validateRequired(rootView.areaIl)
    val streetValid = rootView.streetEt.validateRequired(rootView.streetIl)
    val buildingValid = rootView.buildingEt.validateRequired(rootView.buildingIl)
    val floorApartmentValid = rootView.floorApartmentEt.validateRequired(rootView.floorApartmentIl)

    isFormValid = addressTitleValid
        && areaValid
        && streetValid
        && buildingValid
        && floorApartmentValid
  }

  private fun TextInputEditText.validateRequired(wrapper: TextInputLayout): Boolean {
    if (this.text.isNullOrBlank()) {
      wrapper.error = "This field is required."
      return false
    } else {
      wrapper.error = null
    }
    return true
  }

  private fun selectImage() {
    pickImage.launch("image/*")
  }

  override fun onApplyAllWindowInsets() {
    rootView.appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    rootView.updatePadding(
      bottom =
      if (DimensionUtils.KEYBOARD_INSET == 0)
        DimensionUtils.BOTTOM_INSET
      else
        DimensionUtils.KEYBOARD_INSET
    )
  }

  companion object {
    private const val LATITUDE_KEY = "LATITUDE_KEY"
    private const val LONGITUDE_KEY = "LONGITUDE_KEY"
    private const val AREA_KEY = "AREA_KEY"
    private const val STREET_KEY = "STREET_KEY"

    fun newInstance(
      latitude: String,
      longitude: String,
      area: String?,
      street: String?
    ): EditAddressFragment {
      val args = Bundle().apply {
        putString(LATITUDE_KEY, latitude)
        putString(LONGITUDE_KEY, longitude)
        putString(AREA_KEY, area)
        putString(STREET_KEY, street)
      }

      val fragment = EditAddressFragment()
      fragment.arguments = args
      return fragment
    }
  }
}