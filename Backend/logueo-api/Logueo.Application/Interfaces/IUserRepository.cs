using Logueo.Domain.Entities;

namespace Logueo.Application.Interfaces
{
    public interface IUserRepository
    {
        Task<User?> GetByEmailAsync(string email);
        Task<User?> GetByIdAsync(string id);
        Task AddAsync(User user);
        Task<bool> AddRoleAsync(string email, string roleName);
        Task<IReadOnlyList<string>> GetRolesAsync(string email);
        Task<IEnumerable<User>> GetAllUsers();
    }
}