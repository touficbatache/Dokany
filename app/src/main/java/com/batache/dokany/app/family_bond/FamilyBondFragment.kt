package com.batache.dokany.app.family_bond

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.*
import com.batache.dokany.api.FirebaseRepository
import com.batache.dokany.app.base.LoadingFragment
import com.batache.dokany.model.adapter.shoppingList
import com.batache.dokany.model.pojo.FamilyBond
import com.batache.dokany.model.pojo.FamilyBondList
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.android.synthetic.main.fragment_family_bond.*
import kotlinx.android.synthetic.main.fragment_family_bond.view.*
import kotlinx.android.synthetic.main.fragment_family_bond_setup_screen.view.*
import kotlinx.android.synthetic.main.fragment_family_bond_welcome_screen.view.*

class FamilyBondFragment : LoadingFragment() {

  private val viewModel: FamilyBondViewModel by viewModels {
    FamilyBondViewModel.Factory(requireActivity().application as DokanyApplication)
  }

  private val controller: ShoppingListsController by lazy { ShoppingListsController() }

  private var isPresentationShown = false

  private var familyBondData: FamilyBond? = null

  override fun getLayout(): Int = R.layout.fragment_family_bond

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    showPresentation()

    //TODO: Uncomment this when this rolls out
//    viewModel.eventFamilyBond.observe(viewLifecycleOwner) {
      onDataFetched()
//
//      when (it) {
//        is FamilyBondViewModel.FamilyBondEvent.Success -> {
//          familyBondData = it.familyBond
//          if (!isPresentationShown) {
//            showPresentation()
//          } else {
//            loadFamilyBond(it.familyBond)
//          }
//        }
//        is FamilyBondViewModel.FamilyBondEvent.Failure -> {
//          if (it.error == FirebaseRepository.ERROR_NO_FAMILY_BOND) {
//            showPresentation()
//          }
//        }
//      }
//    }
//
//    gotItBtn.setOnClickListener {
//      welcomeCard.hideWithFade(250)
//    }
//    manageBtn.setOnClickListener {
//      startActivity(Intent(context, FamilyProfilesActivity::class.java))
//    }
//    shoppingListsRv.setController(controller)
  }

  override fun onFetchData() {
    super.onFetchData()

    //TODO: Uncomment this when this rolls out
//    viewModel.getFamilyBondIfAvailable()
  }

  /**
   * Presentation
   */

  private fun showPresentation() {
    //TODO: Uncomment this when this rolls out
//    isPresentationShown = true
//
//    mainActivity?.apply {
//      showTransparentStatusBar = true
//      hideFABs()
//      hideBottomNav()
//    }

    rootView.presentation.visibility = View.VISIBLE
    rootView.welcomeCard.visibility = View.VISIBLE

    val drawable = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.avd_family_knot)
    rootView.welcomeScreenFamilyKnotIv.setImageDrawable(drawable)
    drawable?.start()

    runIn(3000) {
      rootView.welcomeScreenTopBox.visibility = View.VISIBLE
      rootView.welcomeScreenOverline.showWithFade(500)
      runIn(750) {
        rootView.welcomeScreenTitle.showWithFade(500)
        runIn(1000) {
          //TODO: Uncomment this when this rolls out
//          rootView.welcomeScreenGetStartedBtn.visibility = View.VISIBLE
        }
      }
    }

    rootView.welcomeScreenGetStartedBtn.setOnClickListener {
      MaterialSharedAxis(MaterialSharedAxis.X, true).let { sharedAxis ->
        TransitionManager.beginDelayedTransition(rootView.presentation as ViewGroup, sharedAxis)

        rootView.welcomeScreen.visibility = View.GONE
        rootView.setupScreen.visibility = View.VISIBLE
      }
    }

    rootView.createFamilyBondCard.setOnClickListener {
      rootView.createFamilyBondCard.isChecked = true
      rootView.joinFamilyBondCard.isChecked = false
      rootView.setupScreenNextBtn.isEnabled = true
    }

    rootView.joinFamilyBondCard.setOnClickListener {
      rootView.createFamilyBondCard.isChecked = false
      rootView.joinFamilyBondCard.isChecked = true
      rootView.setupScreenNextBtn.isEnabled = true
    }

    rootView.setupScreenNextBtn.setOnClickListener {
      mainActivity?.apply {
        showTransparentStatusBar = false
        showFABs()
        showBottomNav()
      }
      rootView.presentation.hideWithFade(750)
    }

    familyBondData?.let {
      loadFamilyBond(it)
    }

//    mainActivity?.apply {
//      showTransparentStatusBar = false
//      showFABs()
//      showBottomNav()
//    }
//
//    rootView.presentation.fadeOut(750)
  }

  /**
   * Family Bond
   */

  private fun loadFamilyBond(familyBond: FamilyBond) {
    rootView.familyBondNameOverlineTv.setText(
      if (viewModel.isCurrentUserAdmin(familyBond.admins))
        R.string.you_re_an_admin_of
      else
        R.string.you_re_a_member_of
    )
    rootView.familyBondNameTv.text = familyBond.name
  }

  inner class ShoppingListsController : EpoxyController() {
    private val shoppingLists: MutableList<FamilyBondList> = ArrayList()

    fun setShoppingLists(shoppingLists: List<FamilyBondList>) {
      this.shoppingLists.clear()
      this.shoppingLists.addAll(shoppingLists)
    }

    override fun buildModels() {
      for (shoppingList in shoppingLists) {
        shoppingList {
          id(shoppingList.id)
          shoppingList(shoppingList)
          listener { model, parentView, clickedView, position ->
            shoppingLists[position].id
          }
        }
      }
    }
  }
}