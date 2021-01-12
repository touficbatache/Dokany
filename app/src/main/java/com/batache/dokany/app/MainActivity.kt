package com.batache.dokany.app

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

  private val viewModel: MainViewModel by viewModels {
    MainViewModelFactory(
      (application as DokanyApplication).sellersRepository
    )
  }

  private lateinit var navController: NavController

  private val listener =
    NavController.OnDestinationChangedListener { controller, destination, arguments ->
      when (destination.id) {
        R.id.shopFragment, R.id.favoritesFragment -> cartFab.show()
        R.id.accountFragment -> cartFab.hide()
      }
      // react on change
      // you can check destination.id or destination.label and act based on that
    }

  override fun getLayout(): Int = R.layout.activity_main

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel.connectionLiveData.observe(this, Observer {
      if (it.eventNetworkError) {
        Snackbar.make(
          fragmentContainer,
          "Network error, couldn't update sellers",
          Snackbar.LENGTH_SHORT
        ).show()
      }
    })

    navController = Navigation.findNavController(this, R.id.navHostFragment).apply {
      NavigationUI.setupWithNavController(bottomNavigationView, this)
    }

    cartFab.setOnClickListener {
      startActivity(Intent(this, CartActivity::class.java))
    }
  }

  override fun onResume() {
    super.onResume()
    navController.addOnDestinationChangedListener(listener)
  }

  override fun onApplyAllWindowInsets() {
    statusBarOverlay.updateLayoutParams<ViewGroup.LayoutParams> {
      height = DimensionUtils.TOP_INSET
    }
  }

  fun goBack() {
    navController.popBackStack()
  }

  override fun onPause() {
    navController.removeOnDestinationChangedListener(listener)
    super.onPause()
  }
}