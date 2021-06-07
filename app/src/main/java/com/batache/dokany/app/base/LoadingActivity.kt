package com.batache.dokany.app.base

import android.os.Bundle
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.batache.dokany.R
import kotlinx.android.synthetic.main.layout_loading.*

abstract class LoadingActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_loading)
    layoutInflater.inflate(getLayout(), contentFl, true)
  }

  override fun onResume() {
    super.onResume()

    onFetchData()
    findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)?.let {
      it.setOnRefreshListener {
        onFetchData()
      }
    }
  }

  fun enableLoading() {
//    MaterialFadeThrough().let { fadeThrough ->
//      fadeThrough.duration = 500
//
//      TransitionManager.beginDelayedTransition(window.decorView.findViewById(android.R.id.content) as ViewGroup, fadeThrough)
//
//      loadingFl.visibility = View.GONE
//    }
    loadingFl.visibility = View.VISIBLE
  }

  fun onLoaded() {
//    MaterialFadeThrough().let { fadeThrough ->
//      fadeThrough.duration = 500
//
//      TransitionManager.beginDelayedTransition(window.decorView.findViewById(android.R.id.content) as ViewGroup, fadeThrough)
//
//      loadingFl.visibility = View.GONE
//    }
    loadingFl.visibility = View.GONE
  }

  open fun onFetchData() {

  }

  open fun onDataFetched() {
    findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)?.let {
      it.isRefreshing = false
    }
    onLoaded()
  }

}