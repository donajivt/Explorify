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
        public IMongoCollection<User> Users { get; }

        public MongoContext(MongoOptions opts)
        {
            MongoMapping.Register();

            var client = new MongoClient(opts.ConnectionString);
            var db = client.GetDatabase(opts.Database);
            Users = db.GetCollection<User>(opts.UsersCollection);

            var idx = new CreateIndexModel<User>(
                 Builders<User>.IndexKeys.Ascending(x => x.Username),
                 new CreateIndexOptions { Unique = false });
            Users.Indexes.CreateOne(idx);
        }
    }
}
