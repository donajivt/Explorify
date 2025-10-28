using Explorify.Api.Publications.Application.Dtos;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Application.Interfaces
{
    public interface IPublicationService
    {
        Task<IEnumerable<PublicationDto>> GetAllAsync();
        Task<PublicationDto?> GetByIdAsync(string id);
        Task<PublicationDto?> GetByLocationAsync(string location);
        Task<IEnumerable<PublicationDto>> GetByUserIdAsync(string userId);
        Task CreateAsync(PublicationDto dto, string userId);
        Task<bool> DeleteAsync(string id, string userId);
        Task<bool> UpdateAsync(string id, PublicationDto dto, string userId);
    }
}
