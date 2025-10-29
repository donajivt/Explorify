using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Explorify.Api.Publications.Domain.Entities
{
    public class Media
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = string.Empty;

        [BsonElement("publicId")]
        public string PublicId { get; set; } = string.Empty;

        [BsonElement("url")]
        public string Url { get; set; } = string.Empty;

        [BsonElement("secureUrl")]
        public string SecureUrl { get; set; } = string.Empty;

        [BsonElement("format")]
        public string Format { get; set; } = string.Empty;

        [BsonElement("resourceType")]
        public string ResourceType { get; set; } = string.Empty;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}