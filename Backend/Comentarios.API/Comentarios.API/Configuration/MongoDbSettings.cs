namespace Comentarios.API.Configuration;

public class MongoDbSettings
{
    public string ConnectionString { get; set; } = string.Empty;
    public string Database { get; set; } = string.Empty;
    public string PublicationsCollection { get; set; } = string.Empty;
}