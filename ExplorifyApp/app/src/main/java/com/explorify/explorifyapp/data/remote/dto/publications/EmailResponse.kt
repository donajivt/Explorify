package com.explorify.explorifyapp.data.remote.dto.publications


data class SingleEmail(
val email: String
)

data class ResponseVerify(
    val success: Boolean,
    val status: String,
    val subStatus:String,
    val account: String,
    val domain: String,
    val errorMessage:String?
)
