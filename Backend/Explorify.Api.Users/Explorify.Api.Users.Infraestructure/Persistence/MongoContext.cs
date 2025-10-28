using Explorify.Api.Users.Domain.Entities;
using Explorify.Api.Users.Infrastructure.Persistence;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Users.Infrastructure.Persistence
{
    public class MongoContext
    {
        public IMongoCollection<User> Publication { get; }

        public MongoContext(MongoOptions opts)
        {
            MongoMapping.Register();

            var client = new MongoClient(opts.ConnectionString);
            var db = client.GetDatabase(opts.Database);
            Publication = db.GetCollection<User>(opts.PublicationsCollection);

            var idx = new CreateIndexModel<User>(
                 Builders<User>.IndexKeys.Ascending(x => x.Title),
                 new CreateIndexOptions { Unique = false });
            Publication.Indexes.CreateOne(idx);
        }
    }
}
