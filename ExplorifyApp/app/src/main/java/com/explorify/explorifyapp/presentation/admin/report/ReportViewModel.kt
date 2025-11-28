package com.explorify.explorifyapp.presentation.admin.report

import com.explorify.explorifyapp.domain.repository.ReportRepository
import com.explorify.explorifyapp.domain.repository.PublicationRepository
import androidx.lifecycle.ViewModel
import com.explorify.explorifyapp.data.remote.dto.publications.Report
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.explorify.explorifyapp.data.remote.dto.publications.EmailData
import com.explorify.explorifyapp.presentation.utils.email.buildPublicationDeletedTemplate
//import com.explorify.explorifyapp.data.remote.dto.Publication
import com.explorify.explorifyapp.data.remote.model.Publication
import android.util.Log
import com.explorify.explorifyapp.data.remote.dto.users.UserResponse
import com.explorify.explorifyapp.domain.repository.UserRepository
import retrofit2.Response
/*
class ReportViewModel(
    private val reportRepository: ReportRepository,
    private val emailRepository: PublicationRepository // usado para enviar mail
) : ViewModel() {

    var reportList = mutableStateOf<List<Report>>(emptyList())
        private set

    var selectedReport = mutableStateOf<Report?>(null)
        private set

    var message = mutableStateOf("")
        private set

    fun loadReports(token: String) {
        viewModelScope.launch {
            try {
                reportList.value = reportRepository.getAll(token)
            } catch (e: Exception) {
                message.value = e.message ?: "Error desconocido"
            }
        }
    }

 /*   fun createReport(
        publicationId: String,
        reportedBy: String,
        reason: String,
        description: String,
        token: String,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = CreateReportRequest(
                    publicationId = publicationId,
                    reportedByUserId = reportedBy,
                    reason = reason,
                    description = description
                )

                val result = reportRepository.createReport(request, token)

                if (result.isSuccess) {
                    onDone(true)
                } else {
                    onDone(false)
                }

            } catch (e: Exception) {
                onDone(false)
            }
        }
    }
*/
} */

class ReportViewModel(
    private val reportRepo: ReportRepository,
    private val publicationsRepo: PublicationRepository,
    private val userRepo: UserRepository,
    private val emailRepo: PublicationRepository // ya usas este para emails
) : ViewModel() {

    var reportedItems by mutableStateOf<List<ReportedPublicationItem>>(emptyList())
        private set

    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun load(token: String) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                // 1️⃣ Obtener TODOS los reportes
                val allReports = reportRepo.getAll(token)
                Log.d("ReportViewModel.Reports:","${allReports}")
                // Agrupar por publicationId
                val grouped = allReports.groupBy { it.publicationId }

                val result = mutableListOf<ReportedPublicationItem>()

                for ((publicationId, reports) in grouped) {
                    try{
                    // 2️⃣ Obtener publicación
                    val pub = publicationsRepo.getById(publicationId, token)

                    // 3️⃣ Obtener nombres de quienes reportaron
                    /*val reporterNames = reports.map { report ->
                        userRepo.getUserById(token,report.reportedByUserId)
                    }*/
                        val reporterNames = reports.map { report ->
                            try {
                                val userResponse = userRepo.getUserById(token, report.reportedByUserId)

                                if (!userResponse.isSuccessful) {
                                    "Desconocido"
                                } else {
                                    userResponse.body()?.result?.name ?: "Desconocido"
                                }
                            } catch (e: Exception) {
                                "Desconocido"
                            }
                        }

                        //Log.d("nombres: ","${report}")
                    result.add(
                        ReportedPublicationItem(
                            publication = pub.result,
                            reports = reports,
                            reporterNames = reporterNames
                        )
                    )
                    } catch (e: Exception) {
                        Log.e("ReportViewModel", "Publicación $publicationId no encontrada, saltando...")
                        continue  // saltamos esta publicación y seguimos
                }
                }

                reportedItems = result

            } catch (e: Exception) {
                error = e.message
            }

            loading = false
        }
    }

    fun deletePublication(
        publication: Publication,
        token: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("delete publication","${publication.id}+${token}")
                // 1️⃣ Eliminar publicación
                //publicationsRepo.deleteadmin(publication.id, token)
                Log.d("${publication.userId}","${publication}")
                // 2️⃣ Enviar correo al dueño
                val getpublication = publicationsRepo.getById(publication.id, token)
                Log.d("publication","${getpublication}")
                //val owner = userRepo.getUserById(token,publication.userId )
                //
                val ownerResponse = userRepo.getUserById(token, publication.userId)

                if (!ownerResponse.isSuccessful) {
                    Log.d("owner", "Error obteniendo usuario: ${ownerResponse.message()}")
                    return@launch
                }

                val owner = ownerResponse.body()?.result
                Log.d("owner","${owner}")
                val email = buildPublicationDeletedTemplate(owner!!.name,publication.title,"Infricción a las normas")

                val emailResponse=  emailRepo.sendEmail(
                    EmailData(
                        to = owner.email,
                        subject = "Tu publicación ha sido eliminada",
                        body = email
                    )
                )
                Log.d("email","${email}")

                // Si falla, no borrar publicación
                if (!emailResponse.isSuccessful) {
                    Log.d("${emailResponse.code()}","${emailResponse.message()}")
                    error = "Error al enviar correo: ${emailResponse.message()}"
                    return@launch
                }

                Log.d("DELETE", "Correo enviado a ${owner.email}")

                // 4️⃣ Borrar publicación SOLO SI el correo se envió bien
                publicationsRepo.deleteadmin(publication.id, token)

                Log.d("DELETE", "Publicación eliminada correctamente")

                // 3️⃣ Recargar pantalla
                onDone()

            } catch (e: Exception) {
                error = e.message
            }
        }
    }
}

data class ReportedPublicationItem(
    val publication: Publication,
    val reports: List<Report>,
    val reporterNames: List<String>
)
