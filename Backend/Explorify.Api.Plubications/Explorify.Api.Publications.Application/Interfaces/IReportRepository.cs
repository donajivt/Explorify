using Explorify.Api.Publications.Domain.Entities;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Domain.Interfaces
{
    public interface IReportRepository
    {
        Task<IEnumerable<PublicationReport>> GetAllReportsAsync();
        Task<PublicationReport?> GetReportByIdAsync(string id);
        Task CreateReportAsync(PublicationReport report);
        Task<IEnumerable<PublicationReport>> GetReportsByPublicationIdAsync(string publicationId);
        Task<IEnumerable<PublicationReport>> GetReportsByUserIdAsync(string userId);
    }
}
