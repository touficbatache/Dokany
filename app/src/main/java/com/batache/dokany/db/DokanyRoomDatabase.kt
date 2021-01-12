package com.batache.dokany.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.batache.dokany.db.cart_products.CartProductsDao
import com.batache.dokany.db.favorites.FavoritesDao
import com.batache.dokany.db.product_sellers.ProductSellersDao
import com.batache.dokany.db.products.ProductsDao
import com.batache.dokany.db.sellers.SellersDao
import com.batache.dokany.model.pojo.*
import kotlinx.coroutines.CoroutineScope

@Database(
  entities = [Product::class, ProductSeller::class, Favorite::class, Seller::class, CartProduct::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(StringListConverter::class)
public abstract class DokanyRoomDatabase : RoomDatabase() {

  abstract fun productsDao(): ProductsDao

  abstract fun sellersDao(): SellersDao

  abstract fun productSellersDao(): ProductSellersDao

  abstract fun favoritesDao(): FavoritesDao

  abstract fun cartProductsDao(): CartProductsDao

  companion object {
    // Singleton prevents multiple instances of database opening at the
    // same time.
    @Volatile
    private var INSTANCE: DokanyRoomDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): DokanyRoomDatabase {
      // if the INSTANCE is not null, then return it,
      // if it is, then create the database
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          DokanyRoomDatabase::class.java,
          "dokany_database"
        ).build()
        INSTANCE = instance
        // return instance
        instance
      }
    }
  }

}