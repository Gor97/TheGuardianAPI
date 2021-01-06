package com.example.theguardian.api

import com.example.theguardian.api.articlemodel.ArticleResponse
import com.example.theguardian.api.feedmodel.FeedResponse
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val API_KEY = "447ad7b5-78c4-4470-baf5-15b48278b9f0"

interface NewsService {

    @GET("/search?api-key=$API_KEY")
    fun getNewsFeed(
        @Query("page") pageNumber: Int = 1
    ): Observable<FeedResponse>

    @GET("{article-path}?api-key=$API_KEY")
    fun getArticle(
        @Path("article-path", encoded = true) id: String,
        @Query("show-fields") showFields: String = "bodyText,thumbnail",
        @Query("show-tags") showTags: String = "contributor"
    ): Observable<ArticleResponse>

    
    @GET("/search?api-key=$API_KEY")
    fun getNewsFeedBySection(
        @Query("section") filterBySectionName: String
    ): Observable<FeedResponse>


    @GET("/{feed-path}?api-key=$API_KEY")
    fun getNewsFeedByAuthor(
        @Path("feed-path", encoded = true) authorPath: String
    ): Observable<FeedResponse>

}
