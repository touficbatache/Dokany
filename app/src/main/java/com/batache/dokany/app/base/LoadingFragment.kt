package com.batache.dokany.app.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.batache.dokany.R
import kotlinx.android.synthetic.main.layout_loading.view.*

abstract class LoadingFragment : BaseFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    rootView = inflater.inflate(R.layout.layout_loading, container, false)
    inflater.inflate(getLayout(), rootView.contentFl, true)
    return rootView
  }

  override fun onResume() {
    super.onResume()

    onFetchData()
    rootView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)?.let {
      it.setOnRefreshListener {
        onFetchData()
      }
    }
  }

  fun enableLoading() {
//    MaterialFadeThrough().let { fadeThrough ->
//      fadeThrough.duration = 500
//
//      TransitionManager.beginDelayedTransition(rootView as ViewGroup, fadeThrough)
//
//      rootView.loadingFl.visibility = View.GONE
//    }
    rootView.loadingFl.visibility = View.VISIBLE
  }

  fun onLoaded() {
//    MaterialFadeThrough().let { fadeThrough ->
//      fadeThrough.duration = 500
//
//      TransitionManager.beginDelayedTransition(rootView as ViewGroup, fadeThrough)
//
//      rootView.loadingFl.visibility = View.GONE
//    }
    rootView.loadingFl.visibility = View.GONE
  }

  open fun onFetchData() {

  }

  open fun onDataFetched() {
    rootView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)?.let {
      it.isRefreshing = false
    }
    onLoaded()
  }

}