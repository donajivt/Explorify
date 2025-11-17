using Explorify.Api.User.Domain.Entities;
using Explorify.Api.User.Application.Interfaces;
using Explorify.Api.User.Infrastructure.Persistence;
using MongoDB.Driver;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.User.Infrastructure.Repositories
{
    public class UserRepository : IUserRepository
    {
        private readonly IMongoCollection<Domain.Entities.User> _users;

        public UserRepository(MongoContext context)
        {
            _users = context.Users;
        }

        public async Task<Domain.Entities.User?> GetByIdAsync(string id)
        {
            return await _users.Find(u => u.Id == id).FirstOrDefaultAsync();
        }

        public async Task<Domain.Entities.User?> GetByEmailAsync(string email)
        {
            return await _users.Find(u => u.Email == email).FirstOrDefaultAsync();
        }

        public async Task<IEnumerable<Domain.Entities.User>> GetAllAsync()
        {
            return await _users.Find(_ => true).ToListAsync();
        }

        public async Task CreateAsync(Domain.Entities.User user)
        {
            await _users.InsertOneAsync(user);
        }

        public async Task UpdateAsync(Domain.Entities.User user)
        {
            await _users.ReplaceOneAsync(u => u.Id == user.Id, user);
        }
        public async Task UpdatePasswordAsync(Domain.Entities.User user)
        {
            await _users.ReplaceOneAsync(u => u.Id == user.Id, user);
        }

        public async Task DeleteAsync(string id)
        {
            await _users.DeleteOneAsync(u => u.Id == id);
        }
        public async Task<IEnumerable<Domain.Entities.User>> GetAllUsersAsync()
        {
            return await _users.Find(_ => true).ToListAsync();
        }

        public async Task<Domain.Entities.User?> GetUserByIdAsync(string id)
        {
            return await _users.Find(u => u.Id == id).FirstOrDefaultAsync();
        }
    }
}
