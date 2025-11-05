using Explorify.Api.Publications.Domain.Entities;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Application.Interfaces
{
    public interface IReportService
    {
        /// <summary>
        /// Crea un nuevo reporte asociado a una publicación.
        /// </summary>
        Task CreateReportAsync(string publicationId, string reportedByUserId, string reason, string? description);

        /// <summary>
        /// Obtiene todos los reportes registrados.
        /// </summary>
        Task<IEnumerable<PublicationReport>> GetAllReportsAsync();

        /// <summary>
        /// Obtiene un reporte específico por su identificador.
        /// </summary>
        Task<PublicationReport?> GetReportByIdAsync(string id);

        /// <summary>
        /// Obtiene todos los reportes asociados a una publicación.
        /// </summary>
        Task<IEnumerable<PublicationReport>> GetReportsByPublicationIdAsync(string publicationId);

        /// <summary>
        /// Obtiene todos los reportes creados por un usuario en particular.
        /// </summary>
        Task<IEnumerable<PublicationReport>> GetReportsByUserIdAsync(string userId);
    }
}
