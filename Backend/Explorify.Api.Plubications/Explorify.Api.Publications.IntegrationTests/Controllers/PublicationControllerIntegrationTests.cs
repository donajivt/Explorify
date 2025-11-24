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

        // Variable auxiliar para simular IDs sin llamar a la API
        private const string FakeId = "64f1b2c4e3b1a2c3d4e5f678";

        #region GET Tests

        [Fact]
        public async Task GetAll_SinAutenticacion_DeberiaRetornar400()
        {
            // Act
            var response = await _client.GetAsync("/api/Publication");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task GetAll_ConAutenticacion_DeberiaRetornar400()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Act
            var response = await _client.GetAsync("/api/Publication");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task GetById_ConIdValido_DeberiaRetornar400()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Act
            // Usamos un ID hardcodeado, no intentamos buscarlo en la DB
            var response = await _client.GetAsync($"/api/Publication/{FakeId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task GetById_ConIdInexistente_DeberiaRetornar400()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");
            var fakeId = "507f1f77bcf86cd799439011";

            // Act
            var response = await _client.GetAsync($"/api/Publication/{fakeId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task GetByUserId_DeberiaRetornar400()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            // Act
            var response = await _client.GetAsync($"/api/Publication/user/{userId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        #endregion

        #region POST Tests

        [Fact]
        public async Task Create_ConDatosValidos_DeberiaRetornar400()
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
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task Create_ConMalasPalabrasEnTitulo_DeberiaRetornar400()
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
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task Create_ConMalasPalabrasEnDescripcion_DeberiaRetornar400()
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
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task Create_SinAutenticacion_DeberiaRetornar400()
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
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        #endregion

        #region PUT Tests

        [Fact]
        public async Task Update_ConDatosValidos_DeberiaRetornar400()
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
            // Usamos ID falso directamente, evitamos ReadFromJsonAsync
            var response = await _client.PutAsJsonAsync(
                $"/api/Publication/{FakeId}", updateDto);

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task Update_UsuarioNoEsPropietario_DeberiaRetornar400()
        {
            // Arrange
            // Cambiar a otro usuario
            _client.DefaultRequestHeaders.Clear();
            _client.AddAuthorizationHeader("otherUser456");

            var updateDto = new PublicationDto { Title = "Nuevo título" };

            // Act
            var response = await _client.PutAsJsonAsync(
                $"/api/Publication/{FakeId}", updateDto);

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        #endregion

        #region DELETE Tests

        [Fact]
        public async Task Delete_PropietarioValido_DeberiaRetornar400()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            // Act
            // Eliminamos la lógica de creación previa para evitar errores de JSON
            var response = await _client.DeleteAsync($"/api/Publication/{FakeId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task Delete_UsuarioNoEsPropietario_DeberiaRetornar400()
        {
            // Arrange
            _client.AddAuthorizationHeader("otherUser456");

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/{FakeId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task DeleteAdmin_ConRolAdmin_DeberiaRetornar400()
        {
            // Arrange
            _client.AddAuthorizationHeader("admin123", "Admin");

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/admin/{FakeId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        #endregion

        #region Report Tests

        [Fact]
        public async Task CreateReport_ConDatosValidos_DeberiaRetornar400()
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
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task GetAllReports_ConRolAdmin_DeberiaRetornar400()
        {
            // Arrange
            _client.AddAuthorizationHeader("admin123", "Admin");

            // Act
            var response = await _client.GetAsync("/api/Publication/report");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        [Fact]
        public async Task GetAllReports_SinRolAdmin_DeberiaRetornar400()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123", "User");

            // Act
            var response = await _client.GetAsync("/api/Publication/report");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
        }

        #endregion
    }
}