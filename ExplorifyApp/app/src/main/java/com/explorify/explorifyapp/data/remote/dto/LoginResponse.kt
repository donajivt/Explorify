package com.explorify.explorifyapp.data.remote.dto


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("result")
    val result: Result? = null,

    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String?
)

data class Result(
    @SerializedName("user")
    val user: User,

    @SerializedName("token")
    val token: String
)

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String
)

data class EmailResponse(
    @SerializedName("result")
    val result: List<User>,

    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String?
)