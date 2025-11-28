package com.explorify.explorifyapp.data.remote.model

import com.explorify.explorifyapp.data.remote.dto.publications.EmailData
import com.google.gson.annotations.SerializedName

data class Publication(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val location: String,
    val latitud: String? = null,
    val longitud: String? = null,
    @SerializedName("userId") val userId: String,
    val createdAt: String
)

data class PublicationResponse(
    val result: Publication,
    val isSuccess: Boolean,
    val message: String
)