package com.example.explorifyapp.data.remote.dto.publications

data class CreatePublicationRequest(
    val imageUrl: String,
    val title: String,
    val description: String,
    val location: String,
    val latitud: String?,      // Nuevo
    val longitud: String?,
    val userId: String
)

data class UpdatePublicationRequest(
    val imageUrl: String,
    val title: String,
    val description: String,
    val location: String,
    val latitud: String?,
    val longitud: String?,
    val userId: String
)