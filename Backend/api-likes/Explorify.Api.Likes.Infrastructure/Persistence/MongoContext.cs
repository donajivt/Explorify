using Explorify.Api.Likes.Domain.Entities;
using MongoDB.Driver;

namespace Explorify.Api.Likes.Infrastructure.Persistence
{
    public class MongoContext
    {
        private readonly IMongoDatabase _database;

        public MongoContext(MongoOptions options)
        {
            var client = new MongoClient(options.ConnectionString);
            _database = client.GetDatabase(options.Database);

            // Registrar el mapeo de MongoDB
            MongoMapping.Register();
        }

        public IMongoCollection<Like> Likes =>
            _database.GetCollection<Like>("likes");
    }
}