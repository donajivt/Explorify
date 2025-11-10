using Explorify.Api.User.Domain.Entities;
using Explorify.Api.User.Infrastructure.Persistence;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.User.Infrastructure.Persistence
{
    public class MongoContext
    {
        public IMongoCollection<Domain.Entities.User> Users { get; }

        public MongoContext(MongoOptions opts)
        {
            MongoMapping.Register();

            var client = new MongoClient(opts.ConnectionString);
            var db = client.GetDatabase(opts.Database);
            Users = db.GetCollection<Domain.Entities.User>(opts.UsersCollection);

            var idx = new CreateIndexModel<Domain.Entities.User>(
                 Builders<Domain.Entities.User>.IndexKeys.Ascending(x => x.Name),
                 new CreateIndexOptions { Unique = false });
            Users.Indexes.CreateOne(idx);
        }
    }
}
