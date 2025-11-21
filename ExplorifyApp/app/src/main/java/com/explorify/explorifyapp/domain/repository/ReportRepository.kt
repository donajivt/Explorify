package com.explorify.explorifyapp.domain.repository

import com.explorify.explorifyapp.data.remote.publications.ReportApi
import com.explorify.explorifyapp.data.remote.dto.publications.Report


class ReportRepository(private val api: ReportApi) {

    suspend fun getAll(token: String): List<Report> {
        val response = api.getAll("Bearer $token")
        if (!response.isSuccessful) throw Exception(response.message())

        return response.body() ?: emptyList()
    }

    suspend fun getById(id: String, token: String): Report {
        val response = api.getById(id, "Bearer $token")
        if (!response.isSuccessful) throw Exception(response.message())

        return response.body()!!
    }

    suspend fun getByPublicationId(publicationId: String, token: String): List<Report> {
        val response = api.getPublicationId(publicationId, "Bearer $token")
        if (!response.isSuccessful) throw Exception("Error al obtener reportes")//response.message())

        return response.body() ?: emptyList()
    }

    suspend fun getByUserId(userId: String, token: String): List<Report> {
        val response = api.getUsersReport(userId, "Bearer $token")
        if (!response.isSuccessful) throw Exception(response.message())

        return response.body() ?: emptyList()
    }

    /*suspend fun createReport(request: CreateReportRequest, token: String): SimpleReportResponse {
        val response = api.createReport(request, "Bearer $token")
        if (!response.isSuccessful) throw Exception(response.message())

        return response.body()!!
    }*/
}
