package com.explorify.explorifyapp.domain.usecase.publications

import com.explorify.explorifyapp.domain.repository.PublicationRepositoryImpl
import com.explorify.explorifyapp.data.remote.model.Publication
import com.explorify.explorifyapp.data.remote.model.PublicationResponse


class PublicationUseCases(
    private val repo: PublicationRepositoryImpl
) {
    class GetPublicationsUseCase(private val repo: PublicationRepositoryImpl) {
        suspend operator fun invoke(token: String): List<Publication> =
            repo.getAll(token)
    }

    class GetPublicationByIdUseCase(private val repo: PublicationRepositoryImpl) {
        suspend operator fun invoke(id: String, token: String): PublicationResponse =
            repo.getById(id, token)
    }

    class CreatePublicationUseCase(private val repo: PublicationRepositoryImpl) {
        suspend operator fun invoke(
            imageUrl: String,
            title: String,
            description: String,
            location: String,
            latitud: String?,      // Nuevo
            longitud: String?,
            userId: String,
            token: String
        ): Publication = repo.create(imageUrl, title, description, location,latitud,longitud, userId, token)
    }

    class UpdatePublicationUseCase(private val repo: PublicationRepositoryImpl) {
        suspend operator fun invoke(
            id: String,
            imageUrl: String,
            title: String,
            description: String,
            location: String,
            latitud: String,      // Nuevo
            longitud: String,
            userId: String,
            token: String
        ): Publication = repo.update(id, imageUrl, title, description, location,latitud,longitud, userId, token)
    }

    class DeletePublicationUseCase(private val repo: PublicationRepositoryImpl) {
        suspend operator fun invoke(id: String, token: String) =
            repo.delete(id, token)
    }
}