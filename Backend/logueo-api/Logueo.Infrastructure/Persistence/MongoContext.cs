using Logueo.Domain.Entities;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Logueo.Infrastructure.Persistence
{
    public class MongoContext
    {
        public IMongoCollection<User> Usuarios { get; }

        public MongoContext(MongoOptions opts)
        {
            MongoMapping.Register();

            var client = new MongoClient(opts.ConnectionString);
            var db = client.GetDatabase(opts.Database);
            Usuarios = db.GetCollection<User>(opts.UsersCollection);

            var idx = new CreateIndexModel<User>(
                Builders<User>.IndexKeys.Ascending(x => x.Email),
                new CreateIndexOptions { Unique = true });
            Usuarios.Indexes.CreateOne(idx);
        }
    }
}
