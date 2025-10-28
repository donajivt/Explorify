using Explorify.Api.Publications.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Application.Interfaces
{
    public interface IPublicationRepository
    {
        Task<IEnumerable<Publication>> GetAllAsync();
        Task<Publication?> GetByIdAsync(string id);
        Task<Publication?> GetByLocationAsync(string location);
        Task<IEnumerable<Publication>> GetByUserIdAsync(string userId);
        Task CreateAsync(Publication publication);
        Task DeleteAsync(string id);
        Task UpdateAsync(Publication publication);
    }
}
