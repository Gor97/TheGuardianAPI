package com.example.theguardian.api.feedmodel

import com.example.theguardian.api.articlemodel.Tag

data class Response(
    val currentPage: Int,
    val orderBy: String,
    val pageSize: Int,
    val pages: Int,
    val results: List<Result>,
    val startIndex: Int,
    val status: String,
    val total: Int,
    val userTier: String,
    val tag: Tag?
)