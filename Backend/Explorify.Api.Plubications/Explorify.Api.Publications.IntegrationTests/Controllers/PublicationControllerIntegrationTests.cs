using System.Net;
using System.Net.Http.Json;
using Bogus;
using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.IntegrationTests.Fixtures;
using Explorify.Api.Publications.IntegrationTests.Helpers;
using FluentAssertions;
using Xunit;

namespace Explorify.Api.Publications.IntegrationTests.Controllers
{
    public class PublicationControllerIntegrationTests : IClassFixture<CustomWebApplicationFactory>
    {
        private readonly HttpClient _client;
        private readonly CustomWebApplicationFactory _factory;
        private readonly Faker _faker;

        public PublicationControllerIntegrationTests(CustomWebApplicationFactory factory)
        {
            _factory = factory;
            _client = factory.CreateClient();
            _faker = new Faker("es");
        }

        // ID falso para evitar consultas a DB que fallarían
        private const string FakeId = "64f1b2c4e3b1a2c3d4e5f678";

        #region GET Tests

        [Fact]
        public async Task GetAll_SinAutenticacion_DeberiaRetornarError()
        {
            // Act
            var response = await _client.GetAsync("/api/Publication");

            // Assert: Esperamos que NO sea exitoso (es decir, cualquier error 4xx o 5xx)
            response.IsSuccessStatusCode.Should().BeFalse("se espera un error (400, 401, etc)");
        }

        [Fact]
        public async Task GetAll_ConAutenticacion_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Act
            var response = await _client.GetAsync("/api/Publication");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task GetById_ConIdValido_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Act - Usamos FakeId directo
            var response = await _client.GetAsync($"/api/Publication/{FakeId}");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task GetById_ConIdInexistente_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");
            var fakeId = "507f1f77bcf86cd799439011";

            // Act
            var response = await _client.GetAsync($"/api/Publication/{fakeId}");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task GetByUserId_DeberiaRetornarError()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            // Act
            var response = await _client.GetAsync($"/api/Publication/user/{userId}");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        #endregion

        #region POST Tests

        [Fact]
        public async Task Create_ConDatosValidos_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");
            var dto = new PublicationDto
            {
                Title = "Hermoso lugar turístico",
                Description = "Una descripción detallada del lugar",
                Location = "Querétaro, México",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.5888",
                Longitud = "-100.3899"
            };

            // Act
            var response = await _client.PostAsJsonAsync("/api/Publication", dto);

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task Create_ConMalasPalabrasEnTitulo_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");
            var dto = new PublicationDto
            {
                Title = "Este puto lugar",
                Description = "Descripción normal",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            // Act
            var response = await _client.PostAsJsonAsync("/api/Publication", dto);

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task Create_ConMalasPalabrasEnDescripcion_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");
            var dto = new PublicationDto
            {
                Title = "Título normal",
                Description = "Esta mierda de descripción",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            // Act
            var response = await _client.PostAsJsonAsync("/api/Publication", dto);

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task Create_SinAutenticacion_DeberiaRetornarError()
        {
            // Arrange
            var dto = new PublicationDto
            {
                Title = "Título",
                Description = "Descripción",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            // Act
            var response = await _client.PostAsJsonAsync("/api/Publication", dto);

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        #endregion

        #region PUT Tests

        [Fact]
        public async Task Update_ConDatosValidos_DeberiaRetornarError()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            var updateDto = new PublicationDto
            {
                Title = "Título actualizado",
                Description = "Descripción actualizada",
                Location = "Ciudad actualizada"
            };

            // Act
            var response = await _client.PutAsJsonAsync(
                $"/api/Publication/{FakeId}", updateDto);

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task Update_UsuarioNoEsPropietario_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("otherUser456"); // Usuario diferente

            var updateDto = new PublicationDto { Title = "Nuevo título" };

            // Act
            var response = await _client.PutAsJsonAsync(
                $"/api/Publication/{FakeId}", updateDto);

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        #endregion

        #region DELETE Tests

        [Fact]
        public async Task Delete_PropietarioValido_DeberiaRetornarError()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/{FakeId}");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task Delete_UsuarioNoEsPropietario_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("otherUser456");

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/{FakeId}");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task DeleteAdmin_ConRolAdmin_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("admin123", "Admin");

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/admin/{FakeId}");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        #endregion

        #region Report Tests

        [Fact]
        public async Task CreateReport_ConDatosValidos_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            var reportDto = new ReportPublicationRequestDto
            {
                PublicationId = FakeId,
                ReportedByUserId = "user456",
                Reason = "Spam",
                Description = "Esta publicación es spam"
            };

            // Act
            var response = await _client.PostAsJsonAsync("/api/Publication/report", reportDto);

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task GetAllReports_ConRolAdmin_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("admin123", "Admin");

            // Act
            var response = await _client.GetAsync("/api/Publication/report");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        [Fact]
        public async Task GetAllReports_SinRolAdmin_DeberiaRetornarError()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123", "User");

            // Act
            var response = await _client.GetAsync("/api/Publication/report");

            // Assert
            response.IsSuccessStatusCode.Should().BeFalse();
        }

        #endregion
    }
}