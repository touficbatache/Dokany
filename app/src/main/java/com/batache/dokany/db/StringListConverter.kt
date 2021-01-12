package com.batache.dokany.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
  @TypeConverter
  fun toStringList(string: String?): List<String>? {
    return string?.let {
      Gson().fromJson<List<String>>(
        it,
        object : TypeToken<List<String>>() {}.rawType
      )
    }
  }

  @TypeConverter
  fun toString(stringList: List<String>?): String? {
    return if (stringList == null) null else Gson().toJson(stringList)
  }
}
