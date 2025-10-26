package com.example.explorifyapp.data.remote.room

import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "auth_token")
data class AuthToken(
    @PrimaryKey val id: Int = 0, // solo uno
    val token: String,
    val username: String,
    val userId: String,
    val userEmail: String,
    val role:String
)
