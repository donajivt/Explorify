using Explorify.Api.Likes.Application.Dtos;
using Explorify.Api.Likes.Application.Interfaces;
using Explorify.Api.Likes.Domain.Entities;

namespace Explorify.Api.Likes.Application.Services
{
    public class LikeService : ILikeService
    {
        private readonly ILikeRepository _repository;

        public LikeService(ILikeRepository repository)
        {
            _repository = repository;
        }

        public async Task<LikeDto> AddLikeAsync(string publicationId, string userId)
        {
            // Verificar si ya existe el like
            var existingLike = await _repository.GetByUserAndPublicationAsync(userId, publicationId);
            if (existingLike != null)
            {
                // Ya tiene like, retornar el existente
                return new LikeDto
                {
                    Id = existingLike.Id,
                    PublicationId = existingLike.PublicationId,
                    UserId = existingLike.UserId,
                    CreatedAt = existingLike.CreatedAt
                };
            }

            // Crear nuevo like
            var like = new Like
            {
                PublicationId = publicationId,
                UserId = userId,
                CreatedAt = DateTime.UtcNow
            };

            await _repository.CreateAsync(like);

            return new LikeDto
            {
                Id = like.Id,
                PublicationId = like.PublicationId,
                UserId = like.UserId,
                CreatedAt = like.CreatedAt
            };
        }

        public async Task<bool> RemoveLikeAsync(string publicationId, string userId)
        {
            var like = await _repository.GetByUserAndPublicationAsync(userId, publicationId);
            if (like == null)
                return false;

            await _repository.DeleteByUserAndPublicationAsync(userId, publicationId);
            return true;
        }

        public async Task<LikeStatsDto> GetLikeStatsAsync(string publicationId, string userId)
        {
            var totalLikes = await _repository.CountByPublicationIdAsync(publicationId);
            var userHasLiked = await UserHasLikedAsync(publicationId, userId);

            return new LikeStatsDto
            {
                PublicationId = publicationId,
                TotalLikes = totalLikes,
                UserHasLiked = userHasLiked
            };
        }

        public async Task<IEnumerable<LikeDto>> GetLikesByPublicationAsync(string publicationId)
        {
            var likes = await _repository.GetByPublicationIdAsync(publicationId);
            return likes.Select(l => new LikeDto
            {
                Id = l.Id,
                PublicationId = l.PublicationId,
                UserId = l.UserId,
                CreatedAt = l.CreatedAt
            });
        }

        public async Task<IEnumerable<LikeDto>> GetLikesByUserAsync(string userId)
        {
            var likes = await _repository.GetByUserIdAsync(userId);
            return likes.Select(l => new LikeDto
            {
                Id = l.Id,
                PublicationId = l.PublicationId,
                UserId = l.UserId,
                CreatedAt = l.CreatedAt
            });
        }

        public async Task<bool> UserHasLikedAsync(string publicationId, string userId)
        {
            var like = await _repository.GetByUserAndPublicationAsync(userId, publicationId);
            return like != null;
        }
    }
}