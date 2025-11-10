using Explorify.Api.User.Domain.Entities;
using Microsoft.Build.Utilities;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.User.Application.Interfaces
{
    public interface IUserRepository
    {
        Task<Domain.Entities.User?> GetByIdAsync(string id);
        Task<Domain.Entities.User?> GetByEmailAsync(string email);
        Task<IEnumerable<Domain.Entities.User>> GetAllAsync();
        System.Threading.Tasks.Task CreateAsync(Domain.Entities.User user);
        System.Threading.Tasks.Task UpdateAsync(Domain.Entities.User user);
        System.Threading.Tasks.Task DeleteAsync(string id);
        Task<IEnumerable<Domain.Entities.User>> GetAllUsersAsync();
        Task<Domain.Entities.User?> GetUserByIdAsync(string id);

    }
}
