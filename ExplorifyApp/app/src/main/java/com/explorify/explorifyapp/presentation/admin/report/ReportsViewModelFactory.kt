package com.explorify.explorifyapp.presentation.admin.report

import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.explorify.explorifyapp.domain.repository.ReportRepository
import com.explorify.explorifyapp.domain.repository.UserRepositoryImpl
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.explorify.explorifyapp.domain.repository.PublicationRepository
import com.explorify.explorifyapp.domain.repository.UserRepository

class ReportsViewModelFactory(
    private val reportRepo: ReportRepository,
    private val pubRepo: PublicationRepositoryImpl,
    private val userRepo: UserRepository,
    private val emailRepo:PublicationRepositoryImpl
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(reportRepo, pubRepo, userRepo,emailRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
