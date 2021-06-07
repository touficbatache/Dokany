package com.batache.dokany.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.batache.dokany.DokanyApplication
import com.batache.dokany.R
import com.batache.dokany.app.base.BaseActivity
import com.batache.dokany.app.cart.CartActivity
import com.batache.dokany.app.favorites.FavoritesActivity
import com.batache.dokany.app.orders.OrderDetailActivity
import com.batache.dokany.model.pojo.OngoingOrder
import com.batache.dokany.util.DimensionUtils
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {

  private val viewModel: MainViewModel by viewModels {
    MainViewModel.Factory(application as DokanyApplication)
  }

  var showTransparentStatusBar = false
    set(value) {
      field = value
      onApplyAllWindowInsets()
    }

  private lateinit var navController: NavController

  private val listener =
    NavController.OnDestinationChangedListener { controller, destination, arguments ->
      showBottomNav()
      showTransparentStatusBar = false
      when (destination.id) {
        R.id.shopFragment, R.id.recipesFragment, R.id.familyBondFragment, R.id.ordersFragment -> {
          showFABs()
        }
        R.id.accountFragment -> {
          hideFABs()
        }
      }
    }

  private val ongoingOrderListener = object : ValueEventListener {
    override fun onDataChange(dataSnapshot: DataSnapshot) {
      try {
        val ongoingOrder = dataSnapshot.getValue<OngoingOrder>()
        if (ongoingOrder != null) {
          ongoingOrderBar.visibility = View.VISIBLE
          ongoingOrderBar.setOnClickListener {
            startActivity(OrderDetailActivity.newIntent(this@MainActivity, ongoingOrder.orderId))
          }
        } else {
          ongoingOrderBar.visibility = View.GONE
          ongoingOrderBar.setOnClickListener(null)
        }
      } catch (e: Exception) {
        ongoingOrderBar.visibility = View.GONE
      }
    }

    override fun onCancelled(databaseError: DatabaseError) {
      // Getting Post failed, log a message
//      Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
    }
  }

  override fun getLayout(): Int = R.layout.activity_main

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleScope.launch {
      withContext(Dispatchers.IO) {
        val token = FirebaseInstallations.getInstance().getToken(false).await()
        Log.d("FirebaseRepo", "tgblog this is the token: $token")
      }
    }

    viewModel.eventNetworkError.observe(this, {
      if (it) {
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
      authResultLauncher.launch(Intent(this, CartActivity::class.java))
    }

    favoritesFab.setOnClickListener {
      authResultLauncher.launch(Intent(this, FavoritesActivity::class.java))
    }

    handleIntent(intent)
  }

  fun showFABs() {
    cartFab.show()
    favoritesFab.show()
  }

  fun hideFABs() {
    cartFab.hide()
    favoritesFab.hide()
  }

  fun showBottomNav() {
    bottomNavigationView
      .animate()
      .translationY(0f)
      .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
      .setDuration(225)
  }

  fun hideBottomNav() {
    bottomNavigationView
      .animate()
      .translationY(bottomNavigationView.measuredHeight.toFloat())
      .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
      .setDuration(175)
  }

  private fun handleIntent(intent: Intent?) {
    val appLinkAction = intent?.action
    val appLinkData = intent?.data
    executeDeeplink(appLinkAction, appLinkData)
  }

  private fun executeDeeplink(appLinkAction: String?, appLinkData: Uri?) {
    if (appLinkAction == Intent.ACTION_VIEW && appLinkData != null) {
      when (appLinkData.host) {
        "connect_profile" -> {
          MaterialAlertDialogBuilder(this)
            .setMessage("Link this profile to account ${appLinkData.lastPathSegment}?")
            .show()
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.let { newIntent ->
      handleIntent(newIntent)
    }
  }

  override fun onResume() {
    super.onResume()
    navController.addOnDestinationChangedListener(listener)

    FirebaseAuth.getInstance().currentUser?.uid?.let {
      Firebase.database.getReference("ongoingOrders/$it")
        .addValueEventListener(ongoingOrderListener)
    }
  }

  override fun onApplyAllWindowInsets() {
    val topInset = if (showTransparentStatusBar) 0 else DimensionUtils.TOP_INSET
    statusBarOverlay.updateLayoutParams<ViewGroup.LayoutParams> {
      height = topInset
    }
  }

  fun goBack() {
    navController.popBackStack()
  }

  override fun onPause() {
    navController.removeOnDestinationChangedListener(listener)

    FirebaseAuth.getInstance().currentUser?.uid?.let {
      Firebase.database.getReference("ongoingOrders/$it")
        .removeEventListener(ongoingOrderListener)
    }

    super.onPause()
  }
}