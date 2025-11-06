using MongoDB.Bson.Serialization.Attributes;

namespace Comentarios.API.DTOs;

public class CrearComentarioRequest
{
    public string PublicacionId { get; set; } = string.Empty;
    public string Text { get; set; } = string.Empty;
}

public class ComentarioResponse
{

    public string? Id { get; set; }
    public string Text { get; set; } = string.Empty;
    public string UserId { get; set; } = string.Empty;
    public string PublicacionId { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}