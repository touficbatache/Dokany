package com.batache.dokany.app.family_bond

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.LoadingActivity
import com.batache.dokany.model.pojo.isLinkedToAccount
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_cart.contentCl
import kotlinx.android.synthetic.main.activity_family_profiles.*
import kotlinx.android.synthetic.main.app_bar.*

class FamilyProfilesActivity : LoadingActivity() {

  private val viewModel: FamilyBondViewModel by viewModels {
    FamilyBondViewModel.Factory(application as DokanyApplication)
  }

  private var profilePictureViews: MutableList<Int> = ArrayList()
  private var profileLabelViews: MutableList<Int> = ArrayList()
  private var profileLinkedViews: MutableList<Int> = ArrayList()
  private var profileUnlinkedViews: MutableList<Int> = ArrayList()

  override fun getLayout(): Int = R.layout.activity_family_profiles

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(topAppBar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.eventFamilyBond.observe(this) {
      onDataFetched()

      if (it is FamilyBondViewModel.FamilyBondEvent.Success) {
        val members = viewModel.filterOutCurrentUser(it.familyBond.members)
        for ((i, member) in members.withIndex()) {
          findViewById<ImageButton>(profilePictureViews[i]).apply {
            alpha = 1f
            setBackgroundResource(listOf(R.drawable.ic_profile_1, R.drawable.ic_profile_2).shuffled().first())
            setImageDrawable(null)
            // TODO: Load image
            if (!member.isLinkedToAccount()) {
              isClickable = true
              setOnClickListener(::onLinkExistingProfileClick)
            }
          }
          findViewById<TextView>(profileLabelViews[i]).apply {
            visibility = View.VISIBLE
            text = member.name
          }
          if (member.isLinkedToAccount()) {
            findViewById<ImageView>(profileLinkedViews[i]).visibility = View.VISIBLE
          } else {
            findViewById<ImageView>(profileUnlinkedViews[i]).visibility = View.VISIBLE
          }

        }
        profilePictureViews = profilePictureViews.subList(members.size, profilePictureViews.size)

        if (profilePictureViews.isNotEmpty() && profileLabelViews.isNotEmpty()) {
          findViewById<ImageButton>(profilePictureViews[0]).apply {
            alpha = 1f
            isClickable = true
            setOnClickListener(::onCreateNewProfileClick)
          }
        }
      }
    }
  }

  override fun onFetchData() {
    super.onFetchData()

    viewModel.getFamilyBondIfAvailable()
  }

  override fun onDataFetched() {
    super.onDataFetched()

    profilePictureViews = mutableListOf(
      R.id.profileOne,
      R.id.profileTwo,
      R.id.profileThree,
      R.id.profileFour
    )

    profileLabelViews = mutableListOf(
      R.id.profileOneText,
      R.id.profileTwoText,
      R.id.profileThreeText,
      R.id.profileFourText
    )
    profileLinkedViews = mutableListOf(
      R.id.profileOneLinkedIv,
      R.id.profileTwoLinkedIv,
      R.id.profileThreeLinkedIv,
      R.id.profileFourLinkedIv
    )
    profileUnlinkedViews = mutableListOf(
      R.id.profileOneUnlinkedIv,
      R.id.profileTwoUnlinkedIv,
      R.id.profileThreeUnlinkedIv,
      R.id.profileFourUnlinkedIv
    )
  }

  private fun onLinkExistingProfileClick(v: View) {
    MaterialAlertDialogBuilder(this)
      .setTitle("Link existing profile") // resources.getString(R.string.title)
      .setView(R.layout.dialog_qr_code)
      .setNegativeButton("Cancel") { dialog, which ->
        dialog.dismiss()
      }
      .create().apply {
        show()
        val qrCode =
          generateQRCode("dokany://connect_profile/${FirebaseAuth.getInstance().currentUser?.uid}")
        findViewById<ImageView>(R.id.qrCode)?.setImageBitmap(qrCode)
      }
  }

  private fun onCreateNewProfileClick(v: View) {
    MaterialAlertDialogBuilder(this)
      .setTitle("Create and link a new profile") // resources.getString(R.string.title)
      .setView(R.layout.dialog_qr_code)
      .setNegativeButton("Cancel") { dialog, which ->
        dialog.dismiss()
      }
      .create().apply {
        show()
        val qrCode =
          generateQRCode("dokany://connect_profile/${FirebaseAuth.getInstance().currentUser?.uid}")
        findViewById<ImageView>(R.id.qrCode)?.setImageBitmap(qrCode)
      }
  }

  private fun generateQRCode(text: String): Bitmap? {
    val multiFormatWriter = MultiFormatWriter()
    return try {
      val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 500, 500)
      val barcodeEncoder = BarcodeEncoder()
      barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: WriterException) {
      e.printStackTrace()
      null
    }
  }

  override fun onApplyAllWindowInsets() {
    appBar.updatePadding(top = DimensionUtils.TOP_INSET)
    contentCl.updatePadding(bottom = DimensionUtils.BOTTOM_INSET)
  }

  companion object {
    private const val TAG = "FamilyProfilesActivity"
  }
}