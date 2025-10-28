using Explorify.Api.Publications.Application.Interfaces;
using Logueo.Infrastructure.Persistence;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Explorify.Api.Publications.Domain;

namespace Explorify.Api.Publications.Infraestructure.Repositories
{
    public class PublicationRepository : IPublicationRepository
    {
        private readonly IMongoCollection<Domain.Entities.Publication> _collection;

        public PublicationRepository(MongoContext context)
        {
            _collection = context.Publication;
        }

        public async Task<IEnumerable<Domain.Entities.Publication>> GetAllAsync() =>
            await _collection.Find(_ => true).ToListAsync();
        public async Task<Domain.Entities.Publication?> GetByIdAsync(string id) =>
            await _collection.Find(p => p.Id == id).FirstOrDefaultAsync();

        public async Task<Domain.Entities.Publication?> GetByLocationAsync(string location) =>
            await _collection.Find(p => p.Location == location).FirstOrDefaultAsync();
        public async Task<IEnumerable<Domain.Entities.Publication>> GetByUserIdAsync(string userId) =>
            await _collection.Find(p => p.UserId == userId).ToListAsync();

        public async Task CreateAsync(Domain.Entities.Publication publication) =>
            await _collection.InsertOneAsync(publication);

        public async Task DeleteAsync(string id) =>
            await _collection.DeleteOneAsync(p => p.Id == id);
        public async Task UpdateAsync(Domain.Entities.Publication publication) =>
            await _collection.ReplaceOneAsync(p => p.Id == publication.Id, publication);
    }
}
