using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using Explorify.Api.Publications.Domain.Interfaces;
using Explorify.Api.Publications.Infraestructure.Persistence;
using MongoDB.Driver;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Infraestructure.Repositories
{
    public class ReportRepository : IReportRepository
    {
        private readonly IMongoCollection<PublicationReport> _reports;

        public ReportRepository(MongoContext context)
        {
            _reports = context.Reports;
        }

        public async Task<IEnumerable<PublicationReport>> GetAllReportsAsync()
        {
            return await _reports.Find(_ => true).ToListAsync();
        }

        public async Task<PublicationReport?> GetReportByIdAsync(string id)
        {
            return await _reports.Find(r => r.Id == id).FirstOrDefaultAsync();
        }

        public async Task<IEnumerable<PublicationReport>> GetReportsByPublicationIdAsync(string publicationId)
        {
            return await _reports.Find(r => r.PublicationId == publicationId).ToListAsync();
        }

        public async Task<IEnumerable<PublicationReport>> GetReportsByUserIdAsync(string userId)
        {
            return await _reports.Find(r => r.ReportedByUserId == userId).ToListAsync();
        }

        public async Task CreateReportAsync(PublicationReport report)
        {
            await _reports.InsertOneAsync(report);
        }
    }
}
