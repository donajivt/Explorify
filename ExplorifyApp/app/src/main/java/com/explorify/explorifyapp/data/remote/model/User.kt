package com.explorify.explorifyapp.data.remote.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String? = null,
    @SerializedName("cloudinaryPublicId") val cloudinaryPublicId: String? = null
)