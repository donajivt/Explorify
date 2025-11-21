package com.explorify.explorifyapp.data.remote.dto

data class Publication(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val location: String,
    val latitud: String?,     // Nuevo campo
    val longitud: String?,
    val userId: String,
    val createdAt: String
)


data class PublicationsResponse(
    val result: List<Publication>,
    val isSuccess: Boolean,
    val message: String
)


data class PublicationMap(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val location: String,
    val latitud: String,
    val longitud: String,
    val userId: String,
    val createdAt: String
)

data class PublicationsMapResponse(
    val result: List<PublicationMap>,
    val isSuccess: Boolean,
    val message: String
)