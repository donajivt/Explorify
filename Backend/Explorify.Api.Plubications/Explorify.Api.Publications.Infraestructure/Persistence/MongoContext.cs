using Explorify.Api.Publications.Domain.Entities;
using MongoDB.Driver;

namespace Explorify.Api.Publications.Infraestructure.Persistence
{
    public class MongoContext
    {
        public IMongoCollection<Domain.Entities.Publication> Publication { get; }
        public IMongoCollection<PublicationReport> Reports { get; }

        public MongoContext(MongoOptions opts)
        {
            MongoMapping.Register();

            var client = new MongoClient(opts.ConnectionString);
            var db = client.GetDatabase(opts.Database);

            Publication = db.GetCollection<Domain.Entities.Publication>(opts.PublicationsCollection);
            Reports = db.GetCollection<PublicationReport>(opts.ReportsCollection);

            var idx = new CreateIndexModel<Domain.Entities.Publication>(
                Builders<Domain.Entities.Publication>.IndexKeys.Ascending(x => x.Title),
                new CreateIndexOptions { Unique = false });

            Publication.Indexes.CreateOne(idx);
        }
    }
}
