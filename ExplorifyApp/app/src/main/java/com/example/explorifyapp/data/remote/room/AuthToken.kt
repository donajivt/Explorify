package com.example.explorifyapp.data.remote.room



@Entity(tableName = "auth_token")
data class AuthToken(
    @PrimaryKey val id: Int = 0, // solo uno
    val token: String
)
