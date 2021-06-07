package com.batache.dokany.api

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

  private const val region: String = "us-central1"
  private val projectId: String get() = FirebaseApp.getInstance().options.projectId ?: ""

  var debugSettings: DebugSettings? = null

  data class DebugSettings(
    val host: String,
    val port: Int
  )

  private val BASE_URL: String
    get() {
      if (debugSettings != null) {
        return "http://${debugSettings!!.host}:${debugSettings!!.port}/$projectId/$region/api/"
      }
      return "https://$region-$projectId.cloudfunctions.net/api/"
    }

  init {
    debugSettings = DebugSettings("192.168.1.105", 5001)
  }

  private var authToken: String? = null

  private suspend fun getRetrofit(): Retrofit {
    val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()

    if (authToken == null) {
      authToken = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
    }

    httpClient.addInterceptor { chain ->
      val request: Request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $authToken")
        .build()
      chain.proceed(request)
    }

    return Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .client(httpClient.build())
      .build()
  }

  suspend fun getApiInterface(): ApiInterface {
    return getRetrofit().create(ApiInterface::class.java)
  }
}