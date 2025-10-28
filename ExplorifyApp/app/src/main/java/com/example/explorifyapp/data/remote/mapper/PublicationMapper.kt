package com.example.explorifyapp.data.remote.mapper

import com.example.explorifyapp.data.remote.dto.Publication
import com.example.explorifyapp.data.remote.model.Publication as DomainPublication

fun Publication.toDomain() = DomainPublication(
    id = id,
    imageUrl = imageUrl,
    title = title,
    description = description,
    location = location,
    userId = userId,
    createdAt = createdAt
)