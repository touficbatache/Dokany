package com.batache.dokany.permissionmanager

import android.Manifest.permission.*

sealed class Permission(vararg val permissions: String) {
  object Location : Permission(ACCESS_FINE_LOCATION)

  companion object {
    fun from(permission: String) = when (permission) {
      ACCESS_FINE_LOCATION -> Location
      else -> throw IllegalArgumentException("Unknown permission: $permission")
    }
  }
}