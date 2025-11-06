using Explorify.Api.Likes.Application.Interfaces;
using Explorify.Api.Likes.Domain.Entities;
using Explorify.Api.Likes.Infrastructure.Persistence;
using MongoDB.Driver;

namespace Explorify.Api.Likes.Infrastructure.Repositories
{
    public class LikeRepository : ILikeRepository
    {
        private readonly IMongoCollection<Like> _collection;

        public LikeRepository(MongoContext context)
        {
            _collection = context.Likes;
        }

        public async Task<IEnumerable<Like>> GetAllAsync()
        {
            return await _collection.Find(_ => true).ToListAsync();
        }

        public async Task<Like?> GetByIdAsync(string id)
        {
            return await _collection.Find(l => l.Id == id).FirstOrDefaultAsync();
        }

        public async Task<Like?> GetByUserAndPublicationAsync(string userId, string publicationId)
        {
            return await _collection.Find(l => l.UserId == userId && l.PublicationId == publicationId)
                .FirstOrDefaultAsync();
        }

        public async Task<IEnumerable<Like>> GetByPublicationIdAsync(string publicationId)
        {
            return await _collection.Find(l => l.PublicationId == publicationId).ToListAsync();
        }

        public async Task<IEnumerable<Like>> GetByUserIdAsync(string userId)
        {
            return await _collection.Find(l => l.UserId == userId).ToListAsync();
        }

        public async Task<int> CountByPublicationIdAsync(string publicationId)
        {
            return (int)await _collection.CountDocumentsAsync(l => l.PublicationId == publicationId);
        }

        public async Task CreateAsync(Like like)
        {
            await _collection.InsertOneAsync(like);
        }

        public async Task DeleteAsync(string id)
        {
            await _collection.DeleteOneAsync(l => l.Id == id);
        }

        public async Task DeleteByUserAndPublicationAsync(string userId, string publicationId)
        {
            await _collection.DeleteOneAsync(l => l.UserId == userId && l.PublicationId == publicationId);
        }
    }
}