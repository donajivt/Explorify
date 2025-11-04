package com.example.explorifyapp.data.remote.dto

data class MediaResponse(
    val result: MediaResult?,
    val isSuccess: Boolean,
    val message: String
)

data class MediaResult(
    val publicId: String,
    val url: String,
    val secureUrl: String,
    val format: String,
    val resourceType: String
)

data class DeleteResponse(
    val isSuccess: Boolean,
    val message: String
)