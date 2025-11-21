package com.explorify.explorifyapp.data.remote.dto.publications

import com.explorify.explorifyapp.data.remote.dto.PublicationMap

data class EmailRequest(
    val result: EmailData,
    val isSuccess: Boolean,
    val message: String
)
data class EmailData(
    val to: String,
    val subject: String,
    val body: String
)