package com.batache.dokany.permissionmanager

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.batache.dokany.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

class PermissionManager() {

  private var activity: WeakReference<AppCompatActivity>? = null
  private var fragment: WeakReference<Fragment>? = null

  private val requiredPermissions = mutableListOf<Permission>()
  private var rationale: String? = null
  private var callback: (Boolean) -> Unit = {}
  private var detailedCallback: (Map<Permission, Boolean>) -> Unit = {}

  private lateinit var permissionCheck: ActivityResultLauncher<Array<String>>

  companion object {
    fun from(activity: AppCompatActivity) = PermissionManager(activity)
    fun from(fragment: Fragment) = PermissionManager(fragment)
  }

  private constructor(activity: AppCompatActivity) : this() {
    this.activity = WeakReference(activity)
    permissionCheck = this.activity!!.get()!!.registerForActivityResult(RequestMultiplePermissions()) { grantResults ->
      sendResultAndCleanUp(grantResults)
    }
  }

  private constructor(fragment: Fragment) : this() {
    this.fragment = WeakReference(fragment)
    permissionCheck = this.fragment!!.get()!!.registerForActivityResult(RequestMultiplePermissions()) { grantResults ->
      sendResultAndCleanUp(grantResults)
    }
  }

  fun rationale(description: String): PermissionManager {
    rationale = description
    return this
  }

  fun request(vararg permission: Permission): PermissionManager {
    requiredPermissions.addAll(permission)
    return this
  }

  fun checkPermission(callback: (Boolean) -> Unit) {
    this.callback = callback
    handlePermissionRequest()
  }

  fun checkDetailedPermission(callback: (Map<Permission, Boolean>) -> Unit) {
    this.detailedCallback = callback
    handlePermissionRequest()
  }

  private fun handlePermissionRequest() {
    if (activity != null) {
      activity?.get()?.let { activity ->
        when {
          areAllPermissionsGranted(activity) -> sendPositiveResult()
          shouldShowPermissionRationale(activity) -> displayRationale(activity)
          else -> requestPermissions()
        }
      }
    } else {
      fragment?.get()?.let { fragment ->
        when {
          areAllPermissionsGranted(fragment.requireContext()) -> sendPositiveResult()
          shouldShowPermissionRationale(fragment) -> displayRationale(fragment.requireContext())
          else -> requestPermissions()
        }
      }
    }
  }

  private fun displayRationale(context: Context) {
    MaterialAlertDialogBuilder(context)
      .setTitle(context.getString(R.string.dialog_permission_title))
      .setMessage(rationale ?: context.getString(R.string.dialog_permission_default_message))
      .setCancelable(false)
      .setPositiveButton(context.getString(R.string.dialog_permission_button_positive)) { _, _ ->
        requestPermissions()
      }
      .show()
  }

  private fun sendPositiveResult() {
    sendResultAndCleanUp(getPermissionList().associate { it to true })
  }

  private fun sendResultAndCleanUp(grantResults: Map<String, Boolean>) {
    callback(grantResults.all { it.value })
    detailedCallback(grantResults.mapKeys { Permission.from(it.key) })
    cleanUp()
  }

  private fun cleanUp() {
    requiredPermissions.clear()
    rationale = null
    callback = {}
    detailedCallback = {}
  }

  private fun requestPermissions() {
    permissionCheck?.launch(getPermissionList())
  }

  private fun areAllPermissionsGranted(context: Context) =
    requiredPermissions.all { it.isGranted(context) }

  private fun shouldShowPermissionRationale(activity: AppCompatActivity) =
    requiredPermissions.any { it.requiresRationale(activity) }

  private fun shouldShowPermissionRationale(fragment: Fragment) =
    requiredPermissions.any { it.requiresRationale(fragment) }

  private fun getPermissionList() =
    requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

  private fun Permission.isGranted(context: Context) =
    permissions.all { hasPermission(context, it) }

  private fun Permission.requiresRationale(activity: AppCompatActivity) =
    permissions.any {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        activity.shouldShowRequestPermissionRationale(it)
      } else {
        false
      }
    }

  private fun Permission.requiresRationale(fragment: Fragment) =
    permissions.any { fragment.shouldShowRequestPermissionRationale(it) }

  private fun hasPermission(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(
      context,
      permission
    ) == PackageManager.PERMISSION_GRANTED
}