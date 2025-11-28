package com.explorify.explorifyapp.data.remote.dto.publications

data class ReportResponse(
    val data: List<Report>
)

data class  Report(
    val id: String,
    val publicationId: String,
    val reportedByUserId: String,
    val reason: String,
    val description: String,
    val createdAt: String
)

typealias ReportsListResponse = List<Report>

data class CreateReportRequest(
    val publicationId: String,
    val reportedByUserId: String,
    val reason: String,
    val description: String
)
