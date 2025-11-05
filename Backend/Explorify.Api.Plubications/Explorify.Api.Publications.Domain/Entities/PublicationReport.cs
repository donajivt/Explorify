using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace Explorify.Api.Publications.Domain.Entities
{
    public class PublicationReport
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = string.Empty;

        [BsonRepresentation(BsonType.ObjectId)]
        public string PublicationId { get; set; } = string.Empty;

        [BsonRepresentation(BsonType.ObjectId)]
        public string ReportedByUserId { get; set; } = string.Empty;

        public string Reason { get; set; } = string.Empty;
        public string? Description { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
