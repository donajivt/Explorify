package com.example.explorifyapp.data.remote.model

data class Publication(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val location: String,
    val userId: String,
    val createdAt: String
)