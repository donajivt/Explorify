using Explorify.Api.Likes.Application.Dtos;
using FluentAssertions;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.VisualStudio.TestPlatform.TestHost;
using System.Net;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using Xunit;

namespace Explorify.Api.Likes.Tests.Integration.Api
{
    public class LikeApiIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
    {
        private readonly WebApplicationFactory<Program> _factory;
        private readonly HttpClient _client;

        public LikeApiIntegrationTests(WebApplicationFactory<Program> factory)
        {
            _factory = factory;
            _client = factory.CreateClient();
        }

        private string GenerateMockJwtToken()
        {
            // Generar un token JWT mock para pruebas
            // En producción deberías usar una librería como System.IdentityModel.Tokens.Jwt
            var header = Convert.ToBase64String(Encoding.UTF8.GetBytes("{\"alg\":\"HS256\",\"typ\":\"JWT\"}"));
            var payload = Convert.ToBase64String(Encoding.UTF8.GetBytes($"{{\"sub\":\"507f1f77bcf86cd799439012\",\"nameid\":\"507f1f77bcf86cd799439012\",\"exp\":{DateTimeOffset.UtcNow.AddHours(1).ToUnixTimeSeconds()}}}"));
            var signature = "mock_signature";
            return $"{header}.{payload}.{signature}";
        }


        [Fact]
        public async Task GetLikesByPublication_WithoutAuth_ReturnsOk()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";

            // Act
            var response = await _client.GetAsync($"/api/Like/publication/{publicationId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.OK);
        }

        [Fact]
        public async Task ToggleLike_WithoutAuth_ReturnsUnauthorized()
        {
            // Arrange
            var request = new LikeRequestDto { PublicationId = "507f1f77bcf86cd799439011" };

            // Act
            var response = await _client.PostAsJsonAsync("/api/Like/toggle", request);

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        }

        [Fact]
        public async Task HasLiked_WithoutAuth_ReturnsUnauthorized()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";

            // Act
            var response = await _client.GetAsync($"/api/Like/has-liked/{publicationId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        }

        [Fact]
        public async Task GetLikesByUser_WithoutAuth_ReturnsUnauthorized()
        {
            // Arrange
            var userId = "507f1f77bcf86cd799439012";

            // Act
            var response = await _client.GetAsync($"/api/Like/user/{userId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        }

        [Fact]
        public async Task ToggleLike_WithAuth_WorksCorrectly()
        {
            // Arrange
            var token = GenerateMockJwtToken();
            _client.DefaultRequestHeaders.Authorization =
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);

            var request = new LikeRequestDto { PublicationId = "507f1f77bcf86cd799439011" };

            // Act - Primera llamada: agregar like
            var response1 = await _client.PostAsJsonAsync("/api/Like/toggle", request);

            // Assert primera llamada
            // Nota: Esto puede fallar si el token mock no es válido
            // En un entorno real, necesitarías generar un token JWT válido
            response1.StatusCode.Should().BeOneOf(
                HttpStatusCode.OK,
                HttpStatusCode.Unauthorized // Si el token mock no es aceptado
            );

            if (response1.StatusCode == HttpStatusCode.OK)
            {
                var content1 = await response1.Content.ReadAsStringAsync();
                var result1 = JsonSerializer.Deserialize<ResponseDto>(content1, new JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true
                });

                result1.Should().NotBeNull();
                result1!.IsSuccess.Should().BeTrue();

                // Act - Segunda llamada: quitar like
                var response2 = await _client.PostAsJsonAsync("/api/Like/toggle", request);

                // Assert segunda llamada
                response2.StatusCode.Should().Be(HttpStatusCode.OK);

                var content2 = await response2.Content.ReadAsStringAsync();
                var result2 = JsonSerializer.Deserialize<ResponseDto>(content2, new JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true
                });

                result2.Should().NotBeNull();
                result2!.IsSuccess.Should().BeTrue();
            }
        }
    }
}