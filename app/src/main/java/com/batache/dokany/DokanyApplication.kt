package com.batache.dokany

import android.app.Application
import com.batache.dokany.db.*
import com.batache.dokany.db.cart_products.CartProductsRepository
import com.batache.dokany.db.favorites.FavoritesRepository
import com.batache.dokany.db.product_sellers.ProductSellersRepository
import com.batache.dokany.db.products.ProductsRepository
import com.batache.dokany.db.sellers.SellersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DokanyApplication : Application() {
  // No need to cancel this scope as it'll be torn down with the process
  val applicationScope = CoroutineScope(SupervisorJob())

  // Using by lazy so the database and the repository are only created when they're needed
  // rather than when the application starts
  val database by lazy { DokanyRoomDatabase.getDatabase(this, applicationScope) }
  val productsRepository by lazy {
    ProductsRepository(
      database.productsDao()
    )
  }
  val sellersRepository by lazy {
    SellersRepository(
      database.sellersDao()
    )
  }
  val productSellersRepository by lazy {
    ProductSellersRepository(
      database.productSellersDao()
    )
  }
  val favoritesRepository by lazy {
    FavoritesRepository(
      database.favoritesDao()
    )
  }
  val cartProductsRepository by lazy {
    CartProductsRepository(
      database.cartProductsDao()
    )
  }
}