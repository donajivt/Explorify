using Comentarios.API.Controllers;
using Comentarios.API.DTOs;
using Comentarios.API.Models;
using Comentarios.API.Services;
using Comentarios.API.Configuration;
using FluentAssertions;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Moq;
using System.Security.Claims;
using Xunit;

namespace Comentarios.API.Tests.Unit;

public class ComentariosControllerTests
{
    private readonly Mock<MongoDbService> _mockMongoService;
    private readonly Mock<ILogger<ComentariosController>> _mockLogger;
    private readonly Mock<IModerationService> _mockModerationService;
    private readonly ComentariosController _controller;

    public ComentariosControllerTests()
    {
        var mockSettings = new Mock<IOptions<MongoDbSettings>>();
        mockSettings.Setup(s => s.Value).Returns(new MongoDbSettings
        {
            ConnectionString = "mongodb://dummy:27017",
            Database = "TestDb",
            PublicationsCollection = "Pubs",
            CommentsCollection = "Comms"
        });

        _mockMongoService = new Mock<MongoDbService>(mockSettings.Object);
        _mockLogger = new Mock<ILogger<ComentariosController>>();
        _mockModerationService = new Mock<IModerationService>();

        _controller = new ComentariosController(
            _mockMongoService.Object,
            _mockLogger.Object,
            _mockModerationService.Object
        );
    }

    [Fact]
    public async Task ObtenerComentarios_PublicacionIdVacio_RetornaBadRequest()
    {
        var resultado = await _controller.ObtenerComentarios("");
        resultado.Result.Should().BeOfType<BadRequestObjectResult>();
    }

    [Fact]
    public async Task ObtenerComentarios_PublicacionNoExiste_RetornaNotFound()
    {
        var publicacionId = "123456789012345678901234";
        _mockMongoService.Setup(m => m.PublicacionExiste(publicacionId))
            .ReturnsAsync(false);

        var resultado = await _controller.ObtenerComentarios(publicacionId);
        resultado.Result.Should().BeOfType<NotFoundObjectResult>();
    }

    [Fact]
    public async Task ObtenerComentarios_PublicacionExiste_RetornaListaDeComentarios()
    {
        var publicacionId = "123456789012345678901234";
        var comentarios = new List<Comentario>
        {
            new Comentario
            {
                Id = "111111111111111111111111",
                Text = "Comentario 1",
                UserId = "user1",
                PublicacionId = publicacionId,
                CreatedAt = DateTime.UtcNow
            },
            new Comentario
            {
                Id = "222222222222222222222222",
                Text = "Comentario 2",
                UserId = "user2",
                PublicacionId = publicacionId,
                CreatedAt = DateTime.UtcNow
            }
        };

        _mockMongoService.Setup(m => m.PublicacionExiste(publicacionId)).ReturnsAsync(true);
        _mockMongoService.Setup(m => m.ObtenerComentarios(publicacionId)).ReturnsAsync(comentarios);

        var resultado = await _controller.ObtenerComentarios(publicacionId);

        resultado.Result.Should().BeOfType<OkObjectResult>();
        var okResult = resultado.Result as OkObjectResult;
        var lista = okResult!.Value as List<ComentarioResponse>;
        lista.Should().HaveCount(2);
    }

    [Fact]
    public async Task CrearComentario_TextoVacio_RetornaBadRequest()
    {
        var request = new CrearComentarioRequest { Text = "", PublicacionId = "123456789012345678901234" };
        var resultado = await _controller.CrearComentario(request);
        resultado.Result.Should().BeOfType<BadRequestObjectResult>();
    }

    [Fact]
    public async Task CrearComentario_TextoMuyLargo_RetornaBadRequest()
    {
        var request = new CrearComentarioRequest { Text = new string('a', 1001), PublicacionId = "123456789012345678901234" };
        var resultado = await _controller.CrearComentario(request);
        resultado.Result.Should().BeOfType<BadRequestObjectResult>();
    }

    [Fact]
    public async Task CrearComentario_ContenidoOfensivo_RetornaBadRequest()
    {
        var request = new CrearComentarioRequest { Text = "Este es un comentario ofensivo", PublicacionId = "123456789012345678901234" };

        _mockModerationService.Setup(m => m.AnalyzeAsync(request.Text)).ReturnsAsync(new ModerationResponse
        {
            IsOffensive = true,
            Flags = new[] { "ofensivo" },
            Score = 0.8,
            Reason = "Contenido inapropiado"
        });

        var resultado = await _controller.CrearComentario(request);
        resultado.Result.Should().BeOfType<BadRequestObjectResult>();
    }

    [Fact]
    public async Task CrearComentario_PublicacionNoExiste_RetornaNotFound()
    {
        var request = new CrearComentarioRequest { Text = "Comentario válido", PublicacionId = "123456789012345678901234" };

        _mockModerationService.Setup(m => m.AnalyzeAsync(request.Text)).ReturnsAsync(new ModerationResponse { IsOffensive = false });
        _mockMongoService.Setup(m => m.PublicacionExiste(request.PublicacionId)).ReturnsAsync(false);

        var resultado = await _controller.CrearComentario(request);

        resultado.Result.Should().BeOfType<NotFoundObjectResult>();
    }

    [Fact]
    public async Task ContarComentarios_PublicacionIdVacio_RetornaBadRequest()
    {
        var resultado = await _controller.ContarComentarios("");
        resultado.Result.Should().BeOfType<BadRequestObjectResult>();
    }

    [Fact]
    public async Task ContarComentarios_PublicacionValida_RetornaConteo()
    {
        var publicacionId = "123456789012345678901234";
        var conteo = 5L;

        _mockMongoService.Setup(m => m.ContarComentarios(publicacionId)).ReturnsAsync(conteo);

        var resultado = await _controller.ContarComentarios(publicacionId);

        resultado.Result.Should().BeOfType<OkObjectResult>();
        var okResult = resultado.Result as OkObjectResult;
        okResult!.Value.Should().NotBeNull();
    }

    [Fact]
    public async Task EliminarComentario_ComentarioEliminado_RetornaOk()
    {
        var comentarioId = "123456789012345678901234";
        var userId = "user1";

        var claims = new List<Claim> { new Claim(ClaimTypes.NameIdentifier, userId) };
        var identity = new ClaimsIdentity(claims, "TestAuth");
        var claimsPrincipal = new ClaimsPrincipal(identity);
        _controller.ControllerContext = new ControllerContext { HttpContext = new DefaultHttpContext { User = claimsPrincipal } };

        _mockMongoService.Setup(m => m.EliminarComentario(comentarioId, userId)).ReturnsAsync(true);

        var resultado = await _controller.EliminarComentario(comentarioId);
        resultado.Should().BeOfType<OkObjectResult>();
    }

    [Fact]
    public async Task EliminarComentario_ComentarioNoEncontrado_RetornaNotFound()
    {
        var comentarioId = "123456789012345678901234";
        var userId = "user1";

        var claims = new List<Claim> { new Claim(ClaimTypes.NameIdentifier, userId) };
        var identity = new ClaimsIdentity(claims, "TestAuth");
        var claimsPrincipal = new ClaimsPrincipal(identity);
        _controller.ControllerContext = new ControllerContext { HttpContext = new DefaultHttpContext { User = claimsPrincipal } };

        _mockMongoService.Setup(m => m.EliminarComentario(comentarioId, userId)).ReturnsAsync(false);

        var resultado = await _controller.EliminarComentario(comentarioId);
        resultado.Should().BeOfType<NotFoundObjectResult>();
    }
}
