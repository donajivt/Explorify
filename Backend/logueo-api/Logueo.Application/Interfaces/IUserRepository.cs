using Logueo.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Logueo.Application.Interfaces
{
    public interface IUserRepository
    {
        Task<User?> GetByEmailAsync(string email);
        Task AddAsync(User user);
        Task<bool> AddRoleAsync(string email, string roleName);
        Task<IReadOnlyList<string>> GetRolesAsync(string email);
    }
}
