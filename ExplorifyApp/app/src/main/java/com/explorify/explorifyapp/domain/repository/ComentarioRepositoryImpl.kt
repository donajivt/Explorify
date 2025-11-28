package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.model.Comentario
import com.explorify.explorifyapp.data.remote.publications.ComentariosApiService
import com.explorify.explorifyapp.data.remote.publications.CreateComentarioRequest
import retrofit2.HttpException

class ComentarioRepositoryImpl(private val api: ComentariosApiService) {

    suspend fun getComentarios(publicacionId: String, token: String): List<Comentario> {
        val response = api.getAll(publicacionId, "Bearer $token")
        if (!response.isSuccessful) throw HttpException(response)

        val comentarios = response.body() ?: emptyList()
        println("✅ Comentarios recibidos: ${comentarios.size}")
        return comentarios
    }

    suspend fun createComentario(publicacionId: String, text: String, token: String): Comentario {
        println("📤 Enviando comentario -> publicacionId=$publicacionId | texto=$text")

        val request = CreateComentarioRequest(publicacionId, text)
        println("📦 JSON ENVIADO: ${com.google.gson.Gson().toJson(request)}")

        val response = api.create(request, "Bearer $token")
        println("📥 Código HTTP = ${response.code()}")
        println("📥 Cuerpo bruto (errorBody) = ${response.errorBody()?.string()}")
        println("📥 Cuerpo normal (body) = ${response.body()}")

        if (!response.isSuccessful && response.code() != 201) {
            println("❌ Error HTTP: ${response.code()} ${response.message()}")
            throw HttpException(response)
        }

        val body = response.body()

        return when {
            // Caso normal, el backend devuelve el objeto creado
            body?.isSuccess == true && body.result != null -> {
                println("🟢 Comentario creado correctamente con id=${body.result.id}")
                body.result
            }

            // Caso en que el backend devuelve 201 sin cuerpo
            response.code() == 201 -> {
                println("⚠️ El servidor devolvió 201 sin cuerpo, pero asumimos que el comentario fue creado.")
                // Creamos un objeto local temporal para mostrarlo sin romper la UI
                Comentario(
                    id = System.currentTimeMillis().toString(),
                    userId = "yo",
                    publicacionId = publicacionId,
                    text = text,
                    createdAt = java.time.OffsetDateTime.now().toString()
                )
            }

            else -> {
                println("⚠️ El servidor devolvió un cuerpo inesperado: $body")
                throw Exception(body?.message ?: "Error al crear comentario")
            }
        }
    }


    suspend fun deleteComentario(id: String, token: String) {
        val response = api.delete(id, "Bearer $token")
        if (!response.isSuccessful) throw HttpException(response)

        val body = response.body()
        if (body?.isSuccess == true) {
            println("🗑️ Comentario eliminado correctamente: id=$id")
        } else {
            throw Exception(body?.message ?: "Error al eliminar comentario")
        }
    }

    suspend fun getCount(publicacionId: String, token: String): Int {
        val response = api.getCount(
            publicacionId = publicacionId,
            token = "Bearer $token"
        )

        if (!response.isSuccessful) throw HttpException(response)

        val body = response.body()
        return body?.count ?: 0
    }
}
