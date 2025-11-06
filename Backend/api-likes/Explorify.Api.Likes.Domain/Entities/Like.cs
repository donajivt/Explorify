using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Explorify.Api.Likes.Domain.Entities
{
    public class Like
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = string.Empty;

        [BsonElement("publicationId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string PublicationId { get; set; } = string.Empty;

        [BsonElement("userId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string UserId { get; set; } = string.Empty;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}