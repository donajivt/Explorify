using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using Comentarios.API.Data;
using Comentarios.API.DTOs;
using Comentarios.API.Models;
using Comentarios.API.Services;

namespace Comentarios.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ComentariosController : ControllerBase
{
    private readonly ComentariosDbContext _context;
    private readonly MongoDbService _mongoService;
    private readonly ILogger<ComentariosController> _logger;

    public ComentariosController(
        ComentariosDbContext context,
        MongoDbService mongoService,
        ILogger<ComentariosController> logger)
    {
        _context = context;
        _mongoService = mongoService;
        _logger = logger;
    }

    [HttpGet]
    public async Task<ActionResult<List<ComentarioResponse>>> ObtenerComentarios([FromQuery] string publicacionId)
    {
        if (string.IsNullOrEmpty(publicacionId))
        {
            return BadRequest("El PublicacionId es requerido");
        }

        // Verificar que la publicación existe en MongoDB
        var publicacionExiste = await _mongoService.PublicacionExiste(publicacionId);
        if (!publicacionExiste)
        {
            return NotFound("La publicación no existe");
        }

        var comentarios = await _context.Comentarios
            .Where(c => c.PublicacionId == publicacionId)
            .OrderByDescending(c => c.FechaCreacion)
            .Select(c => new ComentarioResponse
            {
                Id = c.Id,
                Texto = c.Texto,
                UsuarioId = c.UsuarioId,
                FechaCreacion = c.FechaCreacion
            })
            .ToListAsync();

        return Ok(comentarios);
    }

    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ComentarioResponse>> CrearComentario([FromBody] CrearComentarioRequest request)
    {
        if (string.IsNullOrEmpty(request.Texto) || string.IsNullOrEmpty(request.PublicacionId))
        {
            return BadRequest("Texto y PublicacionId son requeridos");
        }

        // Verificar que la publicación existe en MongoDB
        var publicacionExiste = await _mongoService.PublicacionExiste(request.PublicacionId);
        if (!publicacionExiste)
        {
            return NotFound("La publicación no existe");
        }

        // Extraer el UsuarioId del token JWT
        var usuarioId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value
                       ?? User.FindFirst("sub")?.Value;

        if (string.IsNullOrEmpty(usuarioId))
        {
            _logger.LogWarning("No se pudo extraer el UsuarioId del token");
            return Unauthorized("Token inválido");
        }

        var comentario = new Comentario
        {
            Texto = request.Texto,
            PublicacionId = request.PublicacionId,
            UsuarioId = usuarioId,
            FechaCreacion = DateTime.UtcNow
        };

        _context.Comentarios.Add(comentario);
        await _context.SaveChangesAsync();

        var response = new ComentarioResponse
        {
            Id = comentario.Id,
            Texto = comentario.Texto,
            UsuarioId = comentario.UsuarioId,
            FechaCreacion = comentario.FechaCreacion
        };

        return CreatedAtAction(nameof(ObtenerComentarios),
            new { publicacionId = comentario.PublicacionId },
            response);
    }
}