using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using Comentarios.API.DTOs;
using Comentarios.API.Models;
using Comentarios.API.Services;

namespace Comentarios.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ComentariosController : ControllerBase
{
    private readonly MongoDbService _mongoService;
    private readonly ILogger<ComentariosController> _logger;

    public ComentariosController(
        MongoDbService mongoService,
        ILogger<ComentariosController> logger)
    {
        _mongoService = mongoService;
        _logger = logger;
    }

    /// <summary>
    /// Obtiene todos los comentarios de una publicación
    /// </summary>
    [HttpGet]
    public async Task<ActionResult<List<ComentarioResponse>>> ObtenerComentarios([FromQuery] string publicacionId)
    {
        if (string.IsNullOrEmpty(publicacionId))
        {
            return BadRequest(new { message = "El publicacionId es requerido" });
        }

        var publicacionExiste = await _mongoService.PublicacionExiste(publicacionId);
        if (!publicacionExiste)
        {
            return NotFound(new { message = "La publicación no existe" });
        }

        var comentarios = await _mongoService.ObtenerComentarios(publicacionId);

        var response = comentarios.Select(c => new ComentarioResponse
        {
            Id = c.Id ?? string.Empty,
            Text = c.Text,
            UserId = c.UserId,
            CreatedAt = c.CreatedAt
        }).ToList();

        return Ok(response);
    }

    /// <summary>
    /// Crea un nuevo comentario en una publicación
    /// </summary>
    [HttpPost]
    public async Task<ActionResult<ComentarioResponse>> CrearComentario([FromBody] CrearComentarioRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Text))
        {
            return BadRequest(new { message = "El texto del comentario es requerido" });
        }

        if (string.IsNullOrWhiteSpace(request.PublicacionId))
        {
            return BadRequest(new { message = "El publicacionId es requerido" });
        }

        if (request.Text.Length > 1000)
        {
            return BadRequest(new { message = "El texto no puede exceder los 1000 caracteres" });
        }

        var publicacionExiste = await _mongoService.PublicacionExiste(request.PublicacionId);
        if (!publicacionExiste)
        {
            return NotFound(new { message = "La publicación no existe" });
        }

        var userId = ObtenerUserIdDelToken();

        if (string.IsNullOrEmpty(userId))
        {
            _logger.LogWarning("No se pudo extraer el UserId del token");
            return Unauthorized(new { message = "Token inválido o userId no encontrado" });
        }

        var comentario = new Comentario
        {
            Text = request.Text.Trim(),
            UserId = userId,
            PublicacionId = request.PublicacionId,
            CreatedAt = DateTime.UtcNow
        };

        var comentarioGuardado = await _mongoService.AgregarComentario(comentario);

        var response = new ComentarioResponse
        {
            Id = comentarioGuardado.Id ?? string.Empty,
            Text = comentarioGuardado.Text,
            UserId = comentarioGuardado.UserId,
            CreatedAt = comentarioGuardado.CreatedAt
        };

        _logger.LogInformation("Comentario {ComentarioId} creado exitosamente por usuario {UserId}",
            response.Id, userId);

        return CreatedAtAction(nameof(ObtenerComentarios),
            new { publicacionId = request.PublicacionId },
            response);
    }

    /// <summary>
    /// Elimina un comentario (solo el dueño puede eliminarlo)
    /// </summary>
    [HttpDelete("{comentarioId}")]
    public async Task<ActionResult> EliminarComentario(string comentarioId)
    {
        var userId = ObtenerUserIdDelToken();
        if (string.IsNullOrEmpty(userId))
        {
            return Unauthorized(new { message = "Token inválido" });
        }

        var eliminado = await _mongoService.EliminarComentario(comentarioId, userId);

        if (!eliminado)
        {
            return NotFound(new { message = "Comentario no encontrado o no tienes permisos para eliminarlo" });
        }

        return Ok(new { message = "Comentario eliminado correctamente" });
    }

    /// <summary>
    /// Obtiene el conteo de comentarios de una publicación
    /// </summary>
    [HttpGet("count")]
    public async Task<ActionResult<object>> ContarComentarios([FromQuery] string publicacionId)
    {
        if (string.IsNullOrEmpty(publicacionId))
        {
            return BadRequest(new { message = "El publicacionId es requerido" });
        }

        var count = await _mongoService.ContarComentarios(publicacionId);
        return Ok(new { publicacionId, count });
    }

    private string? ObtenerUserIdDelToken()
    {
        return User.FindFirst(ClaimTypes.NameIdentifier)?.Value
            ?? User.FindFirst("sub")?.Value
            ?? User.FindFirst("userId")?.Value
            ?? User.FindFirst("id")?.Value
            ?? User.FindFirst("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier")?.Value;
    }

    [HttpGet("test-token")]
    public ActionResult<object> TestToken()
    {
        var userId = ObtenerUserIdDelToken();
        var claims = User.Claims.Select(c => new { c.Type, c.Value }).ToList();

        return Ok(new
        {
            userId,
            claims,
            isAuthenticated = User.Identity?.IsAuthenticated ?? false,
            authType = User.Identity?.AuthenticationType
        });
    }
}