namespace Comentarios.API.Models;

public class Comentario
{
    public string Id { get; set; } = Guid.NewGuid().ToString();
    public string Texto { get; set; } = string.Empty;
    public DateTime FechaCreacion { get; set; } = DateTime.UtcNow;
    public string UsuarioId { get; set; } = string.Empty;
    public string PublicacionId { get; set; } = string.Empty;
}