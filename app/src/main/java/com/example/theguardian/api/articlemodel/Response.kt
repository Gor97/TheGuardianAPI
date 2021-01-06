package com.example.theguardian.api.articlemodel

data class Response(
    val content: Content,
    val status: String,
    val total: Int,
    val userTier: String
)