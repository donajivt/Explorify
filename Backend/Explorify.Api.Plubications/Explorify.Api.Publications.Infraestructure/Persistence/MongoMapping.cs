using MongoDB.Bson;
using MongoDB.Bson.Serialization;
using MongoDB.Bson.Serialization.IdGenerators;
using MongoDB.Bson.Serialization.Serializers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Infrastructure.Persistence
{
    public static class MongoMapping
    {
        private static bool _registered;
        public static void Register()
        {
            if (_registered) return;
            _registered = true;

            BsonClassMap.RegisterClassMap<Explorify.Api.Publications.Domain.Entities.Publication>(cm =>
            {
                cm.AutoMap();
                cm.MapIdMember(x => x.Id)
                  .SetIdGenerator(StringObjectIdGenerator.Instance)
                  .SetSerializer(new StringSerializer(BsonType.ObjectId));
            });
        }
    }
}
