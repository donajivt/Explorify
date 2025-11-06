using MongoDB.Driver;
using Comentarios.API.Configuration;
using Comentarios.API.Models;
using Microsoft.Extensions.Options;

namespace Comentarios.API.Services;

public class MongoDbService
{
    private readonly IMongoCollection<Publicacion> _publicacionesCollection;
    private readonly IMongoCollection<Comentario> _comentariosCollection;

    public MongoDbService(IOptions<MongoDbSettings> mongoSettings)
    {
        var mongoClient = new MongoClient(mongoSettings.Value.ConnectionString);
        var database = mongoClient.GetDatabase(mongoSettings.Value.Database);

        _publicacionesCollection = database.GetCollection<Publicacion>(mongoSettings.Value.PublicationsCollection);
        _comentariosCollection = database.GetCollection<Comentario>(mongoSettings.Value.CommentsCollection);
    }

    public async Task<bool> PublicacionExiste(string publicacionId)
    {
        var count = await _publicacionesCollection
            .CountDocumentsAsync(p => p.Id == publicacionId);
        return count > 0;
    }

    public async Task<List<Comentario>> ObtenerComentarios(string publicacionId)
    {
        return await _comentariosCollection
            .Find(c => c.PublicacionId == publicacionId)
            .SortByDescending(c => c.CreatedAt)
            .ToListAsync();
    }

    public async Task<Comentario> AgregarComentario(Comentario comentario)
    {
        await _comentariosCollection.InsertOneAsync(comentario);
        return comentario;
    }

    public async Task<long> ContarComentarios(string publicacionId)
    {
        return await _comentariosCollection
            .CountDocumentsAsync(c => c.PublicacionId == publicacionId);
    }

    public async Task<bool> EliminarComentario(string comentarioId, string userId)
    {
        var result = await _comentariosCollection.DeleteOneAsync(
            c => c.Id == comentarioId && c.UserId == userId);

        return result.DeletedCount > 0;
    }
}