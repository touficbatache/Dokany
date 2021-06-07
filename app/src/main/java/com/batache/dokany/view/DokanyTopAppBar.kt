package com.batache.dokany.view

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.core.view.iterator
import androidx.core.widget.doOnTextChanged
import com.airbnb.epoxy.EpoxyController
import com.batache.dokany.R
import com.batache.dokany.model.adapter.appBarAction
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.view_dokany_top_app_bar.view.*

class DokanyTopAppBar @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : AppBarLayout(context, attrs, R.attr.actionBarStyle) {

  private var actionClickListener: OnActionClickListener? = null

  private var queryWatcher: TextWatcher? = null

  private var startActionsController: ActionIconsController? = null
  private var endActionsController: ActionIconsController? = null

  var title: String? = null
    set(value) {
      field = value
      nameTv.text = value
    }

  var showLogo: Boolean = false
    set(value) {
      field = value
      logoIv.visibility = if (value) View.VISIBLE else View.GONE

      // Update the title, which will show it or not based on the this boolean.
      updateTitle()
    }

  var showSearch: Boolean = false
    set(value) {
      field = value
      searchEt.visibility = if (value) View.VISIBLE else View.GONE
    }

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

  var scrollFlags = LayoutParams.SCROLL_FLAG_SCROLL
    set(value) {
      field = value

      val params = content.layoutParams as LayoutParams
      params.scrollFlags = value
    }

  init {
    inflate(context, R.layout.view_dokany_top_app_bar, this)

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
      title = getString(R.styleable.DokanyTopAppBar_title)
      showLogo = getBoolean(R.styleable.DokanyTopAppBar_showLogo, false)
      showSearch = getBoolean(R.styleable.DokanyTopAppBar_showSearch, false)
      actions = getResourceId(R.styleable.DokanyTopAppBar_actions, 0)
      scrollFlags = getInt(R.styleable.DokanyTopAppBar_scrollFlags, 0)
      recycle()
    }
  }

  private fun updateTitle() {
    // If the logo is hidden, show the title. If it's shown, show the title as empty.
    nameTv.visibility = if (!showLogo) View.VISIBLE else View.GONE

    // If there is no start icon, space the title a bit more from the start.
    titleExtraSpace.visibility = if (!hasStartActions) View.VISIBLE else View.GONE
  }

  fun setActionClickListener(listener: OnActionClickListener) {
    actionClickListener = listener
  }

  fun addSearchQueryListener(
    action: (
      query: CharSequence?,
      start: Int,
      before: Int,
      count: Int
    ) -> Unit
  ) {
    queryWatcher = searchEt.doOnTextChanged(action)
  }

  fun removeSearchQueryListener() {
    return searchEt.removeTextChangedListener(queryWatcher)
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
        appBarAction {
          id(actionIcon.itemId)
          icon(actionIcon.icon)
          onClickListener { view ->
            actionClickListener?.onTopAppBarActionClick(actionIcon.itemId)
          }
        }
      }
    }

  }

  interface OnActionClickListener {
    fun onTopAppBarActionClick(@IdRes id: Int)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    removeSearchQueryListener()
  }

}