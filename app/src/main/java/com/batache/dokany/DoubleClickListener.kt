package com.batache.dokany

import android.os.Handler
import android.view.View

abstract class DoubleClickListener(val DOUBLE_CLICK_INTERVAL: Long = 200L) : View.OnClickListener {
  /*
   * Handler to process click event.
   */
  private val mHandler: Handler = Handler()

  /*
   * Number of clicks in @DOUBLE_CLICK_INTERVAL interval.
   */
  private var clicks = 0

  /*
   * Flag to check if click handler is busy.
   */
  private var isBusy = false

  override fun onClick(view: View) {
    if (!isBusy) {
      //  Prevent multiple click in this short time
      isBusy = true

      // Increase clicks count
      clicks++
      mHandler.postDelayed({
        if (clicks >= 2) {  // Double tap.
          onDoubleClick(view)
        }
        if (clicks == 1) {  // Single tap
          onSingleClick(view)
        }

        // we need to restore clicks count
        clicks = 0
      }, DOUBLE_CLICK_INTERVAL)
      isBusy = false
    }
  }

  /**
   * Called when the user make a single click.
   */
  abstract fun onSingleClick(view: View?)

  /**
   * Called when the user make a double click.
   */
  abstract fun onDoubleClick(view: View?)
}