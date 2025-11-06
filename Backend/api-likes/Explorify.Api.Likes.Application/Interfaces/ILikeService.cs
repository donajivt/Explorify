using Explorify.Api.Likes.Application.Dtos;

namespace Explorify.Api.Likes.Application.Interfaces
{
    public interface ILikeService
    {
        Task<LikeDto> AddLikeAsync(string publicationId, string userId);
        Task<bool> RemoveLikeAsync(string publicationId, string userId);
        Task<LikeStatsDto> GetLikeStatsAsync(string publicationId, string userId);
        Task<IEnumerable<LikeDto>> GetLikesByPublicationAsync(string publicationId);
        Task<IEnumerable<LikeDto>> GetLikesByUserAsync(string userId);
        Task<bool> UserHasLikedAsync(string publicationId, string userId);
    }
}