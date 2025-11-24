using Explorify.Api.Notifications.Domain.Entities;
using Explorify.Api.Notifications.Infrastructure.Persistence;
using MongoDB.Driver;

namespace Explorify.Api.Notifications.Infrastructure.Persistence
{
    public class MongoContext
    {
        public IMongoCollection<Notification> Notification { get; }

        public MongoContext(MongoOptions opts)
        {
            MongoMapping.Register();

            var client = new MongoClient(opts.ConnectionString);
            var db = client.GetDatabase(opts.Database);

            Notification = db.GetCollection<Notification>(opts.NotificationsCollection);

            var idx = new CreateIndexModel<Notification>(
                Builders<Notification>.IndexKeys.Ascending(x => x.Title),
                new CreateIndexOptions { Unique = false });

            Notification.Indexes.CreateOne(idx);
        }
    }
}
