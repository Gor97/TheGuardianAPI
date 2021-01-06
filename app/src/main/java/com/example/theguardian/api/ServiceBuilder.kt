package com.example.theguardian.api

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


object ServiceBuilder {
    /* ConnectionSpec.MODERN_TLS is the default value */
    private var tlsSpecs = listOf(ConnectionSpec.MODERN_TLS);

/* providing backwards-compatibility for API lower than Lollipop: */
 /*   if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        tlsSpecs = Arrays.asList(ConnectionSpec.COMPATIBLE_TLS)
    }
*/
    var client = OkHttpClient.Builder()
    .connectionSpecs(tlsSpecs)
    .build()

    private val rxAdapter = RxJava2CallAdapterFactory.create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://content.guardianapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(rxAdapter)
        .client(client)
        .build()
        .create(NewsService::class.java)

    fun buildService(): NewsService {
        return retrofit
    }
}