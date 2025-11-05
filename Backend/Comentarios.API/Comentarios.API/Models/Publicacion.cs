using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Comentarios.API.Models;

public class Publicacion
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("titulo")]
    public string Titulo { get; set; } = string.Empty;

    [BsonElement("descripcion")]
    public string Descripcion { get; set; } = string.Empty;

    [BsonElement("usuarioId")]
    public string UsuarioId { get; set; } = string.Empty;

    [BsonElement("fechaCreacion")]
    public DateTime FechaCreacion { get; set; }
}