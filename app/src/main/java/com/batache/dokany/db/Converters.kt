package com.batache.dokany.db

import androidx.room.TypeConverter
import com.batache.dokany.model.pojo.product.ProductSeller
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
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
  fun stringListToString(stringList: List<String>?): String? {
    return if (stringList == null) null else Gson().toJson(stringList)
  }

  @TypeConverter
  fun toProductSeller(string: String?): ProductSeller? {
    return string?.let {
      Gson().fromJson(it, ProductSeller::class.java)
    }
  }

  @TypeConverter
  fun productSellerToString(productSeller: ProductSeller?): String? {
    return if (productSeller == null) null else Gson().toJson(productSeller)
  }

  @TypeConverter
  fun toProductSellerList(string: String?): List<ProductSeller>? {
    return string?.let {
      Gson().fromJson<List<ProductSeller>>(
        it,
        object : TypeToken<List<ProductSeller>>() {}.type
      )
    }
  }

  @TypeConverter
  fun productSellerListToString(productSellerList: List<ProductSeller>?): String? {
    return if (productSellerList == null) null else Gson().toJson(productSellerList)
  }
}