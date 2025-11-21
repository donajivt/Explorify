package com.explorify.explorifyapp.data.remote.auth

import android.util.Base64
import org.json.JSONObject

fun decodeJwtPayload(token: String): JSONObject? {
    return try {
        val parts = token.split(".")
        if (parts.size != 3) return null
        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val decodedString = String(decodedBytes, Charsets.UTF_8)
        JSONObject(decodedString)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
