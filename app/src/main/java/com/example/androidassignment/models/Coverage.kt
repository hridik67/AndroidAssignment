package com.example.androidassignment.models

data class Coverage(
    val id: String,
    val title: String,
    val thumbnail: Thumbnail,
    val coverageURL: String
)

data class Thumbnail(
    val basePath: String,
    val key: String
)
