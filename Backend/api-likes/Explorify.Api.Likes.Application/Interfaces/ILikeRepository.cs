using Explorify.Api.Likes.Domain.Entities;

namespace Explorify.Api.Likes.Application.Interfaces
{
    public interface ILikeRepository
    {
        Task<IEnumerable<Like>> GetAllAsync();
        Task<Like?> GetByIdAsync(string id);
        Task<Like?> GetByUserAndPublicationAsync(string userId, string publicationId);
        Task<IEnumerable<Like>> GetByPublicationIdAsync(string publicationId);
        Task<IEnumerable<Like>> GetByUserIdAsync(string userId);
        Task<int> CountByPublicationIdAsync(string publicationId);
        Task CreateAsync(Like like);
        Task DeleteAsync(string id);
        Task DeleteByUserAndPublicationAsync(string userId, string publicationId);
    }
}