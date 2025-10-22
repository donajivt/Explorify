package com.example.explorifyapp.data.remote.dto

data class Publication(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val location: String,
    val userId: String,
    val createdAt: String
)

data class PublicationsResponse(
    val result: List<Publication>,
    val isSuccess: Boolean,
    val message: String
)

