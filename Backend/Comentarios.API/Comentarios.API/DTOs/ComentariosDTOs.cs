namespace Comentarios.API.DTOs;

public class CrearComentarioRequest
{
    public string PublicacionId { get; set; } = string.Empty;
    public string Texto { get; set; } = string.Empty;
}

public class ComentarioResponse
{
    public string Id { get; set; } = string.Empty;
    public string Texto { get; set; } = string.Empty;
    public string UsuarioId { get; set; } = string.Empty;
    public DateTime FechaCreacion { get; set; }
}