using Logueo.Application.Dtos;
using Logueo.Application.Interfaces;
using Logueo.Domain.Entities;
using Logueo.Infrastructure.Persistence;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Logueo.Infrastructure.Repositories
{
    public class UserRepository : IUserRepository
    {
        private readonly IMongoCollection<User> _col;
        public UserRepository(MongoContext ctx) => _col = ctx.Usuarios;
        public Task<User?> GetByEmailAsync(string email) =>
        _col.Find(u => u.Email == email).FirstOrDefaultAsync();

        public Task AddAsync(User user) => _col.InsertOneAsync(user);

        public async Task<bool> AddRoleAsync(string email, string roleName)
        {
            var update = Builders<User>.Update.AddToSet(u => u.Roles, roleName);
            var res = await _col.UpdateOneAsync(u => u.Email == email, update);
            return res.ModifiedCount > 0;
        }

        public async Task<IReadOnlyList<string>> GetRolesAsync(string email)
        {
            var u = await GetByEmailAsync(email);
            return u?.Roles ?? new List<string>();
        }

        public async Task<IEnumerable<User>> GetAllUsers()
        {
            return (IEnumerable<User>)await _col.Find(_ => true).ToListAsync();
        }
    }
}
