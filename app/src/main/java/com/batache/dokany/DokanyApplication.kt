package com.batache.dokany

import android.app.Application
import com.batache.dokany.api.ApiClient
import com.batache.dokany.db.DokanyRoomDatabase
import com.batache.dokany.db.cart_products.CartProductsRepository
import com.batache.dokany.db.products.ProductsRepository
import com.batache.dokany.db.sellers.SellersRepository

class DokanyApplication : Application() {
  // Using by lazy so the database and the repository are only created when they're needed
  // rather than when the application starts
  val database by lazy { DokanyRoomDatabase.getDatabase(this) }
  val productsRepository by lazy {
    ProductsRepository(database.productsDao())
  }
  val sellersRepository by lazy {
    SellersRepository(database.sellersDao())
  }
  val cartProductsRepository by lazy {
    CartProductsRepository(database.cartProductsDao())
  }
}