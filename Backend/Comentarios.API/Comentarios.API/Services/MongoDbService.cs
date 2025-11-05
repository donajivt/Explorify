using MongoDB.Driver;
using Comentarios.API.Configuration;
using Comentarios.API.Models;
using Microsoft.Extensions.Options;

namespace Comentarios.API.Services;

public class MongoDbService
{
    private readonly IMongoCollection<Publicacion> _publicacionesCollection;

    public MongoDbService(IOptions<MongoDbSettings> mongoSettings)
    {
        var mongoClient = new MongoClient(mongoSettings.Value.ConnectionString);
        var database = mongoClient.GetDatabase(mongoSettings.Value.Database);
        _publicacionesCollection = database.GetCollection<Publicacion>(mongoSettings.Value.PublicationsCollection);
    }

    public async Task<Publicacion?> ObtenerPublicacionPorId(string publicacionId)
    {
        return await _publicacionesCollection
            .Find(p => p.Id == publicacionId)
            .FirstOrDefaultAsync();
    }

    public async Task<bool> PublicacionExiste(string publicacionId)
    {
        var count = await _publicacionesCollection
            .CountDocumentsAsync(p => p.Id == publicacionId);
        return count > 0;
    }
}