package com.example.theguardian.api.articlemodel

data class Content(
    val apiUrl: String,
    val fields: Fields,
    val id: String,
    val isHosted: Boolean,
    val pillarId: String,
    val pillarName: String,
    val sectionId: String,
    val sectionName: String,
    val tags: List<Tag>,
    val type: String,
    val webPublicationDate: String,
    val webTitle: String,
    val webUrl: String
)