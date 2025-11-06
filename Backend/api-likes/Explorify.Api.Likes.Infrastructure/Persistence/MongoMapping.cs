using Explorify.Api.Likes.Domain.Entities;
using MongoDB.Bson;
using MongoDB.Bson.Serialization;
using MongoDB.Bson.Serialization.IdGenerators;
using MongoDB.Bson.Serialization.Serializers;

namespace Explorify.Api.Likes.Infrastructure.Persistence
{
    public static class MongoMapping
    {
        private static bool _registered;

        public static void Register()
        {
            if (_registered) return;
            _registered = true;

            BsonClassMap.RegisterClassMap<Like>(cm =>
            {
                cm.AutoMap();
                cm.MapIdMember(x => x.Id)
                  .SetIdGenerator(StringObjectIdGenerator.Instance)
                  .SetSerializer(new StringSerializer(BsonType.ObjectId));
            });
        }
    }
}