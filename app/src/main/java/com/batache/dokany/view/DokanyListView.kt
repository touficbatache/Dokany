package com.batache.dokany.view

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuInflater
import android.widget.PopupMenu
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.core.view.iterator
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyRecyclerView
import com.batache.dokany.R
import com.batache.dokany.model.adapter.divider
import com.batache.dokany.model.adapter.listItem

class DokanyListView @JvmOverloads constructor(
  baseContext: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : EpoxyRecyclerView(baseContext, attrs, defStyleAttr) {

  @MenuRes
  var items: Int = 0
    set(value) {
      field = value

      if (value == 0) {
        return
      }

      invalidateItems()
    }

  private var itemClickListener: OnItemClickListener? = null

  init {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    context.obtainStyledAttributes(attrs, R.styleable.DokanyListView).apply {
      items = getResourceId(R.styleable.DokanyListView_items, 0)
      recycle()
    }

    layoutManager = LinearLayoutManager(context)
  }

  fun setActionClickListener(listener: OnItemClickListener) {
    itemClickListener = listener
    invalidateItems()
  }

  private fun invalidateItems() {
    val menu: Menu = PopupMenu(context, null).menu
    MenuInflater(context).inflate(items, menu)

    withModels {
      var lastGroupId: Int? = null

      val iterate = menu.iterator()
      while (iterate.hasNext()) {
        iterate.next().let { menuItem ->
          if (lastGroupId != null && menuItem.groupId != lastGroupId) {
            divider {
              id(menuItem.groupId)
            }
          }

          listItem {
            id(menuItem.itemId)
            icon(menuItem.icon)
            title(menuItem.title.toString())
            onClickListener { view ->
              itemClickListener?.onItemClick(menuItem.itemId)
            }
          }

          lastGroupId = menuItem.groupId
        }
      }
    }
  }

  interface OnItemClickListener {
    fun onItemClick(@IdRes id: Int)
  }
}