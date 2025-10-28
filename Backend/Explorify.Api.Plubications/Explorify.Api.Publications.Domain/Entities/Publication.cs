using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace Explorify.Api.Publications.Domain.Entities
{
    public class Publication
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = string.Empty;

        [BsonElement("imageUrl")]
        public string ImageUrl { get; set; } = string.Empty;

        [BsonElement("title")]
        public string Title { get; set; } = string.Empty;

        [BsonElement("description")]
        public string Description { get; set; } = string.Empty;

        [BsonElement("location")]
        public string Location { get; set; } = string.Empty;
        [BsonElement("latitud")]
        public string Latitud { get; set; } = string.Empty;
        [BsonElement("longitud")]
        public string Longitud { get; set; } = string.Empty;

        [BsonElement("userId")]
        public string UserId { get; set; } = string.Empty;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
