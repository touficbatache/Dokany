package com.batache.dokany.view

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.iterator
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.R
import com.batache.dokany.model.adapter.DokanyTopAppBarActionModel_
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.layout_dokany_top_app_bar.view.*

class DokanyTopAppBar @JvmOverloads constructor(
  baseContext: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppBarLayout(baseContext, attrs, defStyleAttr) {

  val context =
    ContextThemeWrapper(baseContext, R.style.Widget_MaterialComponents_AppBarLayout_Surface)

  private var actionClickListener: OnActionClickListener? = null

  private var startActionsController: ActionIconsController? = null
  private var endActionsController: ActionIconsController? = null

//  private var startIcon: Drawable? = null
//    set(value) {
//      field = value
//      startIconIb.setImageDrawable(value)
//      startIconIb.setOnClickListener {
//        actionClickListener?.onActionClick(R.id.startIconIb)
//      }
//
//      // Show or hide the start icon, which will adjust the title's space.
//      startIconIb.visibility = if (value != null) View.VISIBLE else View.GONE
//
//      // Update the title, which will add a bit of space to the title.
//      updateTitle()
//    }

  var title: String? = null
    set(value) {
      field = value
      titleTv.text = value
    }

  var showLogo: Boolean = false
    set(value) {
      field = value
      logoIv.visibility = if (value) View.VISIBLE else View.GONE

      // Update the title, which will show it or not based on the this boolean.
      updateTitle()
    }

//  @MenuRes
//  var actionIconsEnd: Int = 0
//    set(value) {
//      field = value
//
//      if (value == 0) {
//        return
//      }
//
//      val menu: Menu = PopupMenu(context, null).menu
//      MenuInflater(context).inflate(value, menu)
//
//      controller?.setIconsFromMenu(menu)
//      controller?.requestModelBuild()
//
////      val menuBuilderClass = Class.forName("com.android.internal.view.menu.MenuBuilder")
////      val constructor: Constructor<*> = menuBuilderClass.getDeclaredConstructor(Context::class.java)
////      return constructor.newInstance(context) as Menu
//    }

  @MenuRes
  var actions: Int = 0
    set(value) {
      field = value

      if (value == 0) {
        return
      }

      val menu: Menu = PopupMenu(context, null).menu
      MenuInflater(context).inflate(value, menu)

      startActionsController?.setIconsFromMenu(menu)
      startActionsController?.showStartIcons()

      endActionsController?.setIconsFromMenu(menu)
      endActionsController?.showEndIcons()
    }

  private var hasStartActions: Boolean = false

  init {
    inflate(context, R.layout.layout_dokany_top_app_bar, this)

    startActionsController = ActionIconsController().apply {
      actionsStartRv.setController(this)
    }

    endActionsController = ActionIconsController().apply {
      actionsEndRv.setController(this)

    }

    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    context.obtainStyledAttributes(attrs, R.styleable.DokanyTopAppBar).apply {
//      startIcon = getDrawable(R.styleable.DokanyTopAppBar_startIcon)
      title = getString(R.styleable.DokanyTopAppBar_title)
      showLogo = getBoolean(R.styleable.DokanyTopAppBar_showLogo, false)
//      actionIconsEnd = getResourceId(R.styleable.DokanyTopAppBar_actionIconsEnd, 0)
      actions = getResourceId(R.styleable.DokanyTopAppBar_actions, 0)
      recycle()
    }
  }

//  fun setStartIconRes(@DrawableRes iconRes: Int) {
//    startIcon = ResourcesCompat.getDrawable(resources, iconRes, context.theme)
//  }

  private fun updateTitle() {
    // If the logo is hidden, show the title. If it's shown, show the title as empty.
    titleTv.visibility = if (!showLogo) View.VISIBLE else View.GONE

    // If there is no start icon, space the title a bit more from the start.
    titleExtraSpace.visibility = if (!hasStartActions) View.VISIBLE else View.GONE
  }

  fun setActionClickListener(listener: OnActionClickListener) {
    actionClickListener = listener
  }

  inner class ActionIconsController : EpoxyController() {

    private val startActions: MutableList<MenuItem> = ArrayList()
    private val endActions: MutableList<MenuItem> = ArrayList()

    private var showStartActions: Boolean = false
    private var showEndActions: Boolean = false

    val actions: List<MenuItem>
      get() {
        if (showStartActions) {
          return startActions
        } else if (showEndActions) {
          return endActions
        }
        return ArrayList()
      }

    fun setIconsFromMenu(menu: Menu) {
      hasStartActions = false
      startActions.clear()
      endActions.clear()

      val iterate = menu.iterator()
      while (iterate.hasNext()) {
        iterate.next().let { menuItem ->
          if (menuItem.groupId == R.id.start) {
            startActions.add(menuItem)
            hasStartActions = true
          } else if (menuItem.groupId == R.id.end) {
            endActions.add(menuItem)
          }
          return@let
        }
      }
      updateTitle()
    }

    fun showStartIcons() {
      showStartActions = true
      requestModelBuild()
    }

    fun showEndIcons() {
      showEndActions = true
      requestModelBuild()
    }

    override fun buildModels() {
      for (actionIcon in actions) {
        DokanyTopAppBarActionModel_().apply {
          id(actionIcon.itemId)
          icon(actionIcon.icon)
          onClickListener(OnClickListener { actionClickListener?.onTopAppBarActionClick(actionIcon.itemId) })
          addTo(this@ActionIconsController)
        }
      }
    }

  }

  interface OnActionClickListener {
    fun onTopAppBarActionClick(@IdRes id: Int)
  }

}