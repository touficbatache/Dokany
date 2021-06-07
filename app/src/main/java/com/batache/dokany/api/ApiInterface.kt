package com.batache.dokany.api

import com.batache.dokany.model.pojo.FamilyBond
import com.batache.dokany.model.pojo.Order
import com.batache.dokany.model.pojo.Seller
import com.batache.dokany.model.pojo.product.ProductResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

  @GET(PRODUCTS)
  suspend fun getProducts(): Response<List<ProductResponse>>

  @GET(SELLERS)
  suspend fun getSellers(): Response<List<Seller>>

  @GET("$SELLERS/{id}")
  suspend fun getSeller(@Path("id") id: String): Response<Seller>

  @GET(ORDERS)
  suspend fun getOrders(): Response<List<Order>>

  @GET("$ORDERS/{id}")
  suspend fun getOrder(@Path("id") id: String): Response<Order>

  @POST(ORDERS)
  suspend fun placeOrder(@Body order: FirebaseRepository.FirestoreOrder): Response<Any>

  @GET(FAMILY_BOND)
  suspend fun getFamilyBond(): Response<FamilyBond>

  companion object {
    /**
     * Functions Endpoints
     */
    private const val PRODUCTS = "products"
    private const val SELLERS = "sellers"
    private const val ORDERS = "orders"
    private const val FAMILY_BOND = "familyBond"
  }

}