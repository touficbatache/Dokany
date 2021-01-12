package com.batache.dokany

import android.os.SystemClock
import android.view.View

abstract class DoubleClickListener : View.OnClickListener {
  private var doubleClickQualificationSpanInMillis: Long
  private var timestampLastClick: Long

  constructor() {
    doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN
    timestampLastClick = 0
  }

  constructor(doubleClickQualificationSpanInMillis: Long) {
    this.doubleClickQualificationSpanInMillis = doubleClickQualificationSpanInMillis
    timestampLastClick = 0
  }

  override fun onClick(v: View?) {
    if (SystemClock.elapsedRealtime() - timestampLastClick < doubleClickQualificationSpanInMillis) {
      onDoubleClick()
    }
    timestampLastClick = SystemClock.elapsedRealtime()
  }

  abstract fun onDoubleClick()

  companion object {
    // The time in which the second tap should be done in order to qualify as
    // a double click
    private const val DEFAULT_QUALIFICATION_SPAN: Long = 200
  }
}