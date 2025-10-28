using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
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
                UserId = p.UserId,
                CreatedAt = p.CreatedAt
            });
        }
        public async Task<PublicationDto?> GetByIdAsync(string id)
        {
            var p = await _repository.GetByLocationAsync(id);
            if (p == null) return null;

            return new PublicationDto
            {
                Id = p.Id,
                ImageUrl = p.ImageUrl,
                Title = p.Title,
                Description = p.Description,
                Location = p.Location,
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
                UserId = p.UserId,
                CreatedAt = p.CreatedAt
            });
        }

        public async Task CreateAsync(PublicationDto dto, string userId)
        {
            var entity = new Publication
            {
                ImageUrl = dto.ImageUrl ?? string.Empty,
                Title = dto.Title ?? string.Empty,
                Description = dto.Description ?? string.Empty,
                Location = dto.Location ?? string.Empty,
                UserId = userId,
                CreatedAt = DateTime.UtcNow // ← FECHA AUTOMÁTICA
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

            existing.ImageUrl = dto.ImageUrl ?? existing.ImageUrl;
            existing.Title = dto.Title ?? existing.Title;
            existing.Description = dto.Description ?? existing.Description;
            existing.Location = dto.Location ?? existing.Location;

            await _repository.UpdateAsync(existing);
            return true;
        }
    }
}
