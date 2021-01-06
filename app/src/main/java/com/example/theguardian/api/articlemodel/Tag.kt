package com.example.theguardian.api.articlemodel

data class Tag(
    val apiUrl: String,
    val bio: String,
    val firstName: String,
    val id: String,
    val lastName: String,
    val references: List<Any>,
    val type: String,
    val webTitle: String,
    val webUrl: String,
    val bylineImageUrl : String
)