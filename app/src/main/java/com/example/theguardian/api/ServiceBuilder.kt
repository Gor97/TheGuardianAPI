package com.example.theguardian.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {

    private val rxAdapter = RxJava2CallAdapterFactory.create()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://content.guardianapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(rxAdapter)
        .build()
        .create(NewsService::class.java)

    fun buildService(): NewsService {
        return retrofit
    }
}