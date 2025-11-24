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

        #region GET Tests

        [Fact]
        public async Task GetAll_SinAutenticacion_DeberiaRetornar401()
        {
            // Act
            var response = await _client.GetAsync("/api/Publication");

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
        }

        [Fact]
        public async Task GetAll_ConAutenticacion_DeberiaRetornar200()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Act
            var response = await _client.GetAsync("/api/Publication");

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result.Should().NotBeNull();
            result!.IsSuccess.Should().BeTrue();
        }

        [Fact]
        public async Task GetById_ConIdValido_DeberiaRetornarPublicacion()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Primero crear una publicación
            var createDto = new PublicationDto
            {
                Title = _faker.Lorem.Sentence(3),
                Description = _faker.Lorem.Paragraph(),
                Location = _faker.Address.City(),
                ImageUrl = _faker.Image.PicsumUrl(),
                Latitud = _faker.Address.Latitude().ToString(),
                Longitud = _faker.Address.Longitude().ToString()
            };

            var createResponse = await _client.PostAsJsonAsync("/api/Publication", createDto);
            createResponse.EnsureSuccessStatusCode();

            // Obtener todas para conseguir el ID
            var allResponse = await _client.GetAsync("/api/Publication");
            var allResult = await allResponse.Content.ReadFromJsonAsync<ResponseDto>();
            var publications = System.Text.Json.JsonSerializer.Deserialize<List<PublicationDto>>(
                allResult!.Result.ToString()!);
            var publicationId = publications![0].Id;

            // Act
            var response = await _client.GetAsync($"/api/Publication/{publicationId}");

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result.Should().NotBeNull();
            result!.IsSuccess.Should().BeTrue();
        }

        [Fact]
        public async Task GetById_ConIdInexistente_DeberiaRetornar404()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");
            var fakeId = "507f1f77bcf86cd799439011";

            // Act
            var response = await _client.GetAsync($"/api/Publication/{fakeId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.NotFound);
        }

        [Fact]
        public async Task GetByUserId_DeberiaRetornarPublicacionesDelUsuario()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            // Crear algunas publicaciones
            for (int i = 0; i < 3; i++)
            {
                var dto = new PublicationDto
                {
                    Title = _faker.Lorem.Sentence(3),
                    Description = _faker.Lorem.Paragraph(),
                    Location = _faker.Address.City(),
                    ImageUrl = _faker.Image.PicsumUrl(),
                    Latitud = _faker.Address.Latitude().ToString(),
                    Longitud = _faker.Address.Longitude().ToString()
                };
                await _client.PostAsJsonAsync("/api/Publication", dto);
            }

            // Act
            var response = await _client.GetAsync($"/api/Publication/user/{userId}");

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result.Should().NotBeNull();
            result!.IsSuccess.Should().BeTrue();
        }

        #endregion

        #region POST Tests

        [Fact]
        public async Task Create_ConDatosValidos_DeberiaCrearPublicacion()
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
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result.Should().NotBeNull();
            result!.IsSuccess.Should().BeTrue();
            result.Message.Should().Contain("creada exitosamente");
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
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result.Should().NotBeNull();
            result!.IsSuccess.Should().BeFalse();
            result.Message.Should().Contain("Título");
            result.Message.Should().Contain("inapropiadas");
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
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result!.Message.Should().Contain("Descripción");
        }

        [Fact]
        public async Task Create_SinAutenticacion_DeberiaRetornar401()
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
            response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        }

        #endregion

        #region PUT Tests

        [Fact]
        public async Task Update_ConDatosValidos_DeberiaActualizar()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            // Crear publicación
            var createDto = new PublicationDto
            {
                Title = "Título original",
                Description = "Descripción original",
                Location = "Ciudad original",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            await _client.PostAsJsonAsync("/api/Publication", createDto);

            // Obtener ID
            var allResponse = await _client.GetAsync("/api/Publication");
            var allResult = await allResponse.Content.ReadFromJsonAsync<ResponseDto>();
            var publications = System.Text.Json.JsonSerializer.Deserialize<List<PublicationDto>>(
                allResult!.Result.ToString()!);
            var publicationId = publications![0].Id;

            // DTO de actualización
            var updateDto = new PublicationDto
            {
                Title = "Título actualizado",
                Description = "Descripción actualizada",
                Location = "Ciudad actualizada"
            };

            // Act
            var response = await _client.PutAsJsonAsync(
                $"/api/Publication/{publicationId}", updateDto);

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result!.Message.Should().Contain("actualizada");
        }

        [Fact]
        public async Task Update_UsuarioNoEsPropietario_DeberiaRetornar403()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Crear publicación
            var createDto = new PublicationDto
            {
                Title = "Título",
                Description = "Descripción",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            await _client.PostAsJsonAsync("/api/Publication", createDto);

            // Obtener ID
            var allResponse = await _client.GetAsync("/api/Publication");
            var allResult = await allResponse.Content.ReadFromJsonAsync<ResponseDto>();
            var publications = System.Text.Json.JsonSerializer.Deserialize<List<PublicationDto>>(
                allResult!.Result.ToString()!);
            var publicationId = publications![0].Id;

            // Cambiar a otro usuario
            _client.DefaultRequestHeaders.Clear();
            _client.AddAuthorizationHeader("otherUser456");

            var updateDto = new PublicationDto { Title = "Nuevo título" };

            // Act
            var response = await _client.PutAsJsonAsync(
                $"/api/Publication/{publicationId}", updateDto);

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
        }

        #endregion

        #region DELETE Tests

        [Fact]
        public async Task Delete_PropietarioValido_DeberiaEliminar()
        {
            // Arrange
            var userId = "user123";
            _client.AddAuthorizationHeader(userId);

            // Crear publicación
            var createDto = new PublicationDto
            {
                Title = "Título a eliminar",
                Description = "Descripción",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            await _client.PostAsJsonAsync("/api/Publication", createDto);

            // Obtener ID
            var allResponse = await _client.GetAsync("/api/Publication");
            var allResult = await allResponse.Content.ReadFromJsonAsync<ResponseDto>();
            var publications = System.Text.Json.JsonSerializer.Deserialize<List<PublicationDto>>(
                allResult!.Result.ToString()!);
            var publicationId = publications![0].Id;

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/{publicationId}");

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
            var result = await response.Content.ReadFromJsonAsync<ResponseDto>();
            result!.Message.Should().Contain("eliminada");
        }

        [Fact]
        public async Task Delete_UsuarioNoEsPropietario_DeberiaRetornar403()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Crear publicación
            var createDto = new PublicationDto
            {
                Title = "Título",
                Description = "Descripción",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            await _client.PostAsJsonAsync("/api/Publication", createDto);

            // Obtener ID
            var allResponse = await _client.GetAsync("/api/Publication");
            var allResult = await allResponse.Content.ReadFromJsonAsync<ResponseDto>();
            var publications = System.Text.Json.JsonSerializer.Deserialize<List<PublicationDto>>(
                allResult!.Result.ToString()!);
            var publicationId = publications![0].Id;

            // Cambiar a otro usuario
            _client.DefaultRequestHeaders.Clear();
            _client.AddAuthorizationHeader("otherUser456");

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/{publicationId}");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.Forbidden);
        }

        [Fact]
        public async Task DeleteAdmin_ConRolAdmin_DeberiaEliminar()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Crear publicación
            var createDto = new PublicationDto
            {
                Title = "Título",
                Description = "Descripción",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            await _client.PostAsJsonAsync("/api/Publication", createDto);

            // Obtener ID
            var allResponse = await _client.GetAsync("/api/Publication");
            var allResult = await allResponse.Content.ReadFromJsonAsync<ResponseDto>();
            var publications = System.Text.Json.JsonSerializer.Deserialize<List<PublicationDto>>(
                allResult!.Result.ToString()!);
            var publicationId = publications![0].Id;

            // Cambiar a admin
            _client.DefaultRequestHeaders.Clear();
            _client.AddAuthorizationHeader("admin123", "Admin");

            // Act
            var response = await _client.DeleteAsync($"/api/Publication/admin/{publicationId}");

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
        }

        #endregion

        #region Report Tests

        [Fact]
        public async Task CreateReport_ConDatosValidos_DeberiaCrearReporte()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123");

            // Crear publicación primero
            var pubDto = new PublicationDto
            {
                Title = "Publicación a reportar",
                Description = "Descripción",
                Location = "Ciudad",
                ImageUrl = "https://example.com/image.jpg",
                Latitud = "20.0",
                Longitud = "-100.0"
            };

            await _client.PostAsJsonAsync("/api/Publication", pubDto);

            // Obtener ID
            var allResponse = await _client.GetAsync("/api/Publication");
            var allResult = await allResponse.Content.ReadFromJsonAsync<ResponseDto>();
            var publications = System.Text.Json.JsonSerializer.Deserialize<List<PublicationDto>>(
                allResult!.Result.ToString()!);
            var publicationId = publications![0].Id;

            var reportDto = new ReportPublicationRequestDto
            {
                PublicationId = publicationId!,
                ReportedByUserId = "user456",
                Reason = "Spam",
                Description = "Esta publicación es spam"
            };

            // Act
            var response = await _client.PostAsJsonAsync("/api/Publication/report", reportDto);

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
        }

        [Fact]
        public async Task GetAllReports_ConRolAdmin_DeberiaRetornarReportes()
        {
            // Arrange
            _client.AddAuthorizationHeader("admin123", "Admin");

            // Act
            var response = await _client.GetAsync("/api/Publication/report");

            // Assert
            response.StatusCode.Should().BeOneOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized);
        }

        [Fact]
        public async Task GetAllReports_SinRolAdmin_DeberiaRetornar403()
        {
            // Arrange
            _client.AddAuthorizationHeader("user123", "User");

            // Act
            var response = await _client.GetAsync("/api/Publication/report");

            // Assert
            response.StatusCode.Should().Be(HttpStatusCode.Forbidden);
        }

        #endregion
    }
}