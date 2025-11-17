using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Exceptions;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Application.Validators;
using Explorify.Api.Publications.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Application.Services
{
    public class PublicationService : IPublicationService
    {
        private readonly IPublicationRepository _repository;

        public PublicationService(IPublicationRepository repository)
        {
            _repository = repository;
        }

        public async Task<IEnumerable<PublicationDto>> GetAllAsync()
        {
            var publications = await _repository.GetAllAsync();
            return publications.Select(p => new PublicationDto
            {
                Id = p.Id,
                ImageUrl = p.ImageUrl,
                Title = p.Title,
                Description = p.Description,
                Location = p.Location,
                Latitud = p.Latitud,
                Longitud = p.Longitud,
                UserId = p.UserId,
                CreatedAt = p.CreatedAt
            });
        }

        public async Task<PublicationDto?> GetByIdAsync(string id)
        {
            var p = await _repository.GetByIdAsync(id);
            if (p == null) return null;

            return new PublicationDto
            {
                Id = p.Id,
                ImageUrl = p.ImageUrl,
                Title = p.Title,
                Description = p.Description,
                Location = p.Location,
                Latitud = p.Latitud,
                Longitud = p.Longitud,
                UserId = p.UserId,
                CreatedAt = p.CreatedAt
            };
        }

        public async Task<PublicationDto?> GetByLocationAsync(string location)
        {
            var p = await _repository.GetByLocationAsync(location);
            if (p == null) return null;

            return new PublicationDto
            {
                Id = p.Id,
                ImageUrl = p.ImageUrl,
                Title = p.Title,
                Description = p.Description,
                Location = p.Location,
                Latitud = p.Latitud,
                Longitud = p.Longitud,
                UserId = p.UserId,
                CreatedAt = p.CreatedAt
            };
        }

        public async Task<IEnumerable<PublicationDto>> GetByUserIdAsync(string userId)
        {
            var publications = await _repository.GetByUserIdAsync(userId);
            return publications.Select(p => new PublicationDto
            {
                Id = p.Id,
                ImageUrl = p.ImageUrl,
                Title = p.Title,
                Description = p.Description,
                Location = p.Location,
                Latitud = p.Latitud,
                Longitud = p.Longitud,
                UserId = p.UserId,
                CreatedAt = p.CreatedAt
            });
        }

        public async Task CreateAsync(PublicationDto dto, string userId)
        {
            //  Validar malas palabras en Título
            var (titleValid, titleBadWords) = BadWordsValidator.Validate(dto.Title ?? string.Empty);
            if (!titleValid)
            {
                throw new BadWordsException(
                    "Título", titleBadWords);
            }

            //  Validar malas palabras en Descripción
            var (descValid, descBadWords) = BadWordsValidator.Validate(dto.Description ?? string.Empty);
            if (!descValid)
            {
                throw new BadWordsException(
                    "Descripción", descBadWords);
            }

            //  Validar malas palabras en Ubicación
            var (locValid, locBadWords) = BadWordsValidator.Validate(dto.Location ?? string.Empty);
            if (!locValid)
            {
                throw new BadWordsException(
                    "Ubicación", locBadWords);
            }

            var entity = new Publication
            {
                ImageUrl = dto.ImageUrl ?? string.Empty,
                Title = dto.Title ?? string.Empty,
                Description = dto.Description ?? string.Empty,
                Location = dto.Location ?? string.Empty,
                Latitud = dto.Latitud ?? string.Empty,
                Longitud = dto.Longitud ?? string.Empty,
                UserId = userId,
                CreatedAt = DateTime.UtcNow
            };

            await _repository.CreateAsync(entity);
        }

        public async Task<bool> DeleteAsync(string id, string userId)
        {
            var pub = await _repository.GetByIdAsync(id);
            if (pub == null || pub.UserId != userId)
                return false;

            await _repository.DeleteAsync(id);
            return true;
        }

        public async Task<bool> UpdateAsync(string id, PublicationDto dto, string userId)
        {
            var existing = await _repository.GetByIdAsync(id);
            if (existing == null) return false;

            if (existing.UserId != userId) return false;

            // Validar malas palabras si se actualiza el título
            if (!string.IsNullOrWhiteSpace(dto.Title))
            {
                var (titleValid, titleBadWords) = BadWordsValidator.Validate(dto.Title);
                if (!titleValid)
                {
                    throw new BadWordsException(
                        "Título", titleBadWords);
                }
                existing.Title = dto.Title;
            }

            // Validar malas palabras si se actualiza la descripción
            if (!string.IsNullOrWhiteSpace(dto.Description))
            {
                var (descValid, descBadWords) = BadWordsValidator.Validate(dto.Description);
                if (!descValid)
                {
                    throw new BadWordsException(
                        "Descripción", descBadWords);
                }
                existing.Description = dto.Description;
            }

            //  Validar malas palabras si se actualiza la ubicación
            if (!string.IsNullOrWhiteSpace(dto.Location))
            {
                var (locValid, locBadWords) = BadWordsValidator.Validate(dto.Location);
                if (!locValid)
                {
                    throw new BadWordsException(
                        "Ubicación", locBadWords);
                }
                existing.Location = dto.Location;
            }

            // Actualizar otros campos
            if (!string.IsNullOrWhiteSpace(dto.ImageUrl))
                existing.ImageUrl = dto.ImageUrl;

            if (!string.IsNullOrWhiteSpace(dto.Latitud))
                existing.Latitud = dto.Latitud;

            if (!string.IsNullOrWhiteSpace(dto.Longitud))
                existing.Longitud = dto.Longitud;

            await _repository.UpdateAsync(existing);
            return true;
        }
    }
}