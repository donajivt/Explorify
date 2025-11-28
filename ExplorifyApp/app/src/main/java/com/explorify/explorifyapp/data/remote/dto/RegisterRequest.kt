package com.explorify.explorifyapp.data.remote.dto


import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("role")
    val role: String = "USER"
)