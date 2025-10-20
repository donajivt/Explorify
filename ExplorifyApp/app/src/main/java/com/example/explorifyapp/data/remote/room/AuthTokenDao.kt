package com.example.explorifyapp.data.remote.room

import androidx.room.Insert

@Dao
interface AuthTokenDao {
    @Query("SELECT * FROM auth_token LIMIT 1")
    suspend fun getToken(): AuthToken?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: AuthToken)

    @Query("DELETE FROM auth_token")
    suspend fun clearToken()
}
