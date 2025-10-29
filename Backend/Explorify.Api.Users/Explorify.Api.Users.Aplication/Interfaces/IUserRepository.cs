using Explorify.Api.Users.Domain.Entities;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.Users.Aplication.Interfaces
{
    public interface IUserRepository
    {
        Task<User?> GetByIdAsync(string id);
        Task<User?> GetByEmailAsync(string email);
        Task<IEnumerable<User>> GetAllAsync();
        Task CreateAsync(User user);
        Task UpdateAsync(User user);
        Task DeleteAsync(string id);
    }
}
