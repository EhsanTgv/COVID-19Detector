package com.taghavi.covid_19detector.utilities

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private const val BASE_URL = "http://chichiapp.ir:8838/upload/x-ray/"
    private var retrofit: Retrofit? = null
    fun getRetrofitClient(context: Context?): Retrofit? {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient.Builder()
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}