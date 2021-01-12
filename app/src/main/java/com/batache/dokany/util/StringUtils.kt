package com.batache.dokany.util

import android.telephony.PhoneNumberUtils

class StringUtils {
  companion object {
    fun formatLebanesePhoneNumber(phoneNumber: String): String {
      return PhoneNumberUtils.formatNumber(phoneNumber, "LBN")
    }
  }
}