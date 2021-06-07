package com.batache.dokany.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.batache.dokany.db.cart_products.CartProductsDao
import com.batache.dokany.db.products.ProductsDao
import com.batache.dokany.db.sellers.SellersDao
import com.batache.dokany.model.pojo.cart.CartProductJoin
import com.batache.dokany.model.pojo.product.ProductResponse
import com.batache.dokany.model.pojo.Seller

@Database(
  entities = [ProductResponse::class, Seller::class, CartProductJoin::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
public abstract class DokanyRoomDatabase : RoomDatabase() {

  abstract fun productsDao(): ProductsDao

  abstract fun sellersDao(): SellersDao

  abstract fun cartProductsDao(): CartProductsDao

  companion object {
    // Singleton prevents multiple instances of database opening at the
    // same time.
    @Volatile
    private var INSTANCE: DokanyRoomDatabase? = null

    fun getDatabase(context: Context): DokanyRoomDatabase {
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