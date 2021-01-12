package com.batache.dokany

import android.content.Context
import android.widget.SimpleAdapter

class DokanySimpleAdapter(
  context: Context,
  val data: List<Map<String, *>?>,
  resource: Int,
  from: Array<String?>?,
  to: IntArray?
) : SimpleAdapter(context, data, resource, from, to) {
  override fun getItem(position: Int): Any {
    data[position]?.get("price")?.let {
      return it
    }

    return super.getItem(position)
  }

  fun getSellerId(position: Int): String? {
    data[position]?.get("id")?.let {
      return it.toString()
    }
    return null
  }
}