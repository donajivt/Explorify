package com.example.explorifyapp.data.remote.dto.users

import com.google.gson.annotations.SerializedName

    data class UserRequest(
        @SerializedName("username")
        val username: String,

        @SerializedName("email")
        val email: String
    )