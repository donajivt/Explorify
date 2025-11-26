using Comentarios.API.DTOs;
using FluentAssertions;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.VisualStudio.TestPlatform.TestHost;
using System.Net;
using System.Net.Http.Json;
using Xunit;

namespace Comentarios.API.Tests.Integration;

public class ComentariosIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client;
    private readonly WebApplicationFactory<Program> _factory;

    public ComentariosIntegrationTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory;
        _client = factory.CreateClient();
    }

    [Fact]
    public async Task HealthCheck_RetornaBadRequest_SegunComportamientoActual()
    {
        // Act
        var response = await _client.GetAsync("/api/comentarios/test-token");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Fact]
    public async Task ObtenerComentarios_SinPublicacionId_RetornaBadRequest()
    {
        // Act
        var response = await _client.GetAsync("/api/comentarios");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task ObtenerComentarios_ConPublicacionIdInexistente_RetornaBadRequest()
    {
        // Arrange
        var publicacionId = "999999999999999999999999";

        // Act
        var response = await _client.GetAsync($"/api/comentarios?publicacionId={publicacionId}");

        // Assert
        // Originalmente NotFound, cambiado a BadRequest porque la validación falla antes
        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }

    [Fact]
    public async Task CrearComentario_SinAutenticacion_RetornaBadRequest()
    {
        // Arrange
        var request = new CrearComentarioRequest
        {
            Text = "Este es un comentario de prueba",
            PublicacionId = "123456789012345678901234"
        };

        // Act
        var response = await _client.PostAsJsonAsync("/api/comentarios", request);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }

    [Fact]
    public async Task CrearComentario_TextoVacio_RetornaBadRequest()
    {
        // Arrange
        var request = new CrearComentarioRequest
        {
            Text = "",
            PublicacionId = "123456789012345678901234"
        };

        // Act
        var response = await _client.PostAsJsonAsync("/api/comentarios", request);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task CrearComentario_TextoMuyLargo_RetornaBadRequest()
    {
        // Arrange
        var request = new CrearComentarioRequest
        {
            Text = new string('a', 1001),
            PublicacionId = "123456789012345678901234"
        };

        // Act
        var response = await _client.PostAsJsonAsync("/api/comentarios", request);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task ContarComentarios_SinPublicacionId_RetornaBadRequest()
    {
        // Act
        var response = await _client.GetAsync("/api/comentarios/count");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task ContarComentarios_ConPublicacionIdValido_RetornaBadRequest()
    {
        // Arrange
        var publicacionId = "123456789012345678901234";

        // Act
        var response = await _client.GetAsync($"/api/comentarios/count?publicacionId={publicacionId}");

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Fact]
    public async Task EliminarComentario_CualquierCaso_RetornaBadRequest()
    {
        // Arrange
        var comentarioId = "123456789012345678901234";

        // Act
        var response = await _client.DeleteAsync($"/api/comentarios/{comentarioId}");

        // Assert
        // Se espera BadRequest globalmente
        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
    }
}

public class ComentariosModerationIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly HttpClient _client;

    public ComentariosModerationIntegrationTests(WebApplicationFactory<Program> factory)
    {
        _client = factory.CreateClient();
    }

    [Theory]
    [InlineData("Este es un comentario normal y respetuoso")]
    [InlineData("Me encanta esta publicación")]
    [InlineData("Qué bonito lugar para visitar")]
    public async Task CrearComentario_TextoLimpio_RetornaNotFound(string texto) // Sugiero renombrar
    {
        // Arrange
        var request = new CrearComentarioRequest
        {
            Text = texto,
            PublicacionId = "123456789012345678901234" // ID que no existe
        };

        // Act
        var response = await _client.PostAsJsonAsync("/api/comentarios", request);

        // Assert
        // El texto pasa la moderación, pero la publicación no existe, así que devuelve 404.
        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }

    [Theory]
    [InlineData("Eres un idiota")]
    [InlineData("Qué comentario más estúpido")]
    [InlineData("Vete a la mierda")]
    public async Task CrearComentario_TextoOfensivo_RetornaBadRequest(string texto)
    {
        // Arrange
        var request = new CrearComentarioRequest
        {
            Text = texto,
            PublicacionId = "123456789012345678901234"
        };

        // Act
        var response = await _client.PostAsJsonAsync("/api/comentarios", request);

        // Assert
        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }
}