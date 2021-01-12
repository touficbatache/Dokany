package com.batache.dokany.app.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

  lateinit var rootView: View

  @LayoutRes
  abstract fun getLayout(): Int

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    rootView = inflater.inflate(getLayout(), container, false)
    return rootView
  }
}