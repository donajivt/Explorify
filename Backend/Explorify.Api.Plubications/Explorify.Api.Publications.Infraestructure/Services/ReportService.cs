using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using Explorify.Api.Publications.Domain.Interfaces;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Infraestructure.Services
{
    public class ReportService : IReportService
    {
        private readonly IReportRepository _reportRepository;

        public ReportService(IReportRepository reportRepository)
        {
            _reportRepository = reportRepository;
        }

        // ✅ Crear un nuevo reporte
        public async Task CreateReportAsync(string publicationId, string reportedByUserId, string reason, string? description)
        {
            if (string.IsNullOrWhiteSpace(publicationId))
                throw new ArgumentException("El ID de la publicación es obligatorio.", nameof(publicationId));

            if (string.IsNullOrWhiteSpace(reportedByUserId))
                throw new ArgumentException("El ID del usuario es obligatorio.", nameof(reportedByUserId));

            if (string.IsNullOrWhiteSpace(reason))
                throw new ArgumentException("Debe especificarse una razón para el reporte.", nameof(reason));

            var report = new PublicationReport
            {
                PublicationId = publicationId,
                ReportedByUserId = reportedByUserId,
                Reason = reason,
                Description = description ?? string.Empty,
                CreatedAt = DateTime.UtcNow
            };

            await _reportRepository.CreateReportAsync(report);
        }

        // ✅ Obtener todos los reportes
        public async Task<IEnumerable<PublicationReport>> GetAllReportsAsync()
        {
            return await _reportRepository.GetAllReportsAsync();
        }

        // ✅ Obtener un reporte por ID
        public async Task<PublicationReport?> GetReportByIdAsync(string id)
        {
            if (string.IsNullOrWhiteSpace(id))
                throw new ArgumentException("El ID del reporte es obligatorio.", nameof(id));

            return await _reportRepository.GetReportByIdAsync(id);
        }

        // ✅ Obtener reportes por publicación
        public async Task<IEnumerable<PublicationReport>> GetReportsByPublicationIdAsync(string publicationId)
        {
            if (string.IsNullOrWhiteSpace(publicationId))
                throw new ArgumentException("El ID de la publicación es obligatorio.", nameof(publicationId));

            return await _reportRepository.GetReportsByPublicationIdAsync(publicationId);
        }

        // ✅ Obtener reportes por usuario
        public async Task<IEnumerable<PublicationReport>> GetReportsByUserIdAsync(string userId)
        {
            if (string.IsNullOrWhiteSpace(userId))
                throw new ArgumentException("El ID del usuario es obligatorio.", nameof(userId));

            return await _reportRepository.GetReportsByUserIdAsync(userId);
        }
    }
}
