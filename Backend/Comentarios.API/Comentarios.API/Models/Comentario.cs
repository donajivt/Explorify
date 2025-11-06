using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Comentarios.API.Models;

public class Comentario
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("text")]
    public string Text { get; set; } = string.Empty;

    [BsonElement("userId")]
    public string UserId { get; set; } = string.Empty;

    [BsonElement("publicacionId")]
    public string PublicacionId { get; set; } = string.Empty;

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}