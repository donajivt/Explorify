package com.explorify.explorifyapp.data.remote.model

data class Comentario(
    val id: String,
    val text: String,
    val userId: String,
    val publicacionId: String,
    val createdAt: String
)