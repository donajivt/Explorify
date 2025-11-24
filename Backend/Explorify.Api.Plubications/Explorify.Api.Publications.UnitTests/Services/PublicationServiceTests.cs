using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Exceptions;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Application.Services;
using Explorify.Api.Publications.Domain.Entities;
using Explorify.Api.Publications.UnitTests.Helpers;
using FluentAssertions;
using Moq;
using Xunit;

namespace Explorify.Api.Publications.UnitTests.Services
{
    public class PublicationServiceTests
    {
        private readonly Mock<IPublicationRepository> _repositoryMock;
        private readonly PublicationService _service;

        public PublicationServiceTests()
        {
            _repositoryMock = new Mock<IPublicationRepository>();
            _service = new PublicationService(_repositoryMock.Object);
        }

        #region GetAllAsync Tests

        [Fact]
        public async Task GetAllAsync_DeberiaRetornarTodasLasPublicaciones()
        {
            // Arrange
            var publications = TestDataBuilder.CreatePublications(5);
            _repositoryMock.Setup(r => r.GetAllAsync())
                .ReturnsAsync(publications);

            // Act
            var result = await _service.GetAllAsync();

            // Assert
            result.Should().HaveCount(5);
            result.Should().AllBeOfType<PublicationDto>();
            _repositoryMock.Verify(r => r.GetAllAsync(), Times.Once);
        }

        [Fact]
        public async Task GetAllAsync_CuandoNoHayPublicaciones_DeberiaRetornarListaVacia()
        {
            // Arrange
            _repositoryMock.Setup(r => r.GetAllAsync())
                .ReturnsAsync(new List<Publication>());

            // Act
            var result = await _service.GetAllAsync();

            // Assert
            result.Should().BeEmpty();
        }

        #endregion

        #region GetByIdAsync Tests

        [Fact]
        public async Task GetByIdAsync_ConIdValido_DeberiaRetornarPublicacion()
        {
            // Arrange
            var publication = TestDataBuilder.CreatePublication();
            _repositoryMock.Setup(r => r.GetByIdAsync(publication.Id))
                .ReturnsAsync(publication);

            // Act
            var result = await _service.GetByIdAsync(publication.Id);

            // Assert
            result.Should().NotBeNull();
            result!.Id.Should().Be(publication.Id);
            result.Title.Should().Be(publication.Title);
        }

        [Fact]
        public async Task GetByIdAsync_ConIdInexistente_DeberiaRetornarNull()
        {
            // Arrange
            _repositoryMock.Setup(r => r.GetByIdAsync(It.IsAny<string>()))
                .ReturnsAsync((Publication?)null);

            // Act
            var result = await _service.GetByIdAsync("id-inexistente");

            // Assert
            result.Should().BeNull();
        }

        #endregion

        #region GetByUserIdAsync Tests

        [Fact]
        public async Task GetByUserIdAsync_DeberiaRetornarPublicacionesDelUsuario()
        {
            // Arrange
            var userId = "user123";
            var publications = new List<Publication>
            {
                TestDataBuilder.CreatePublication(userId: userId),
                TestDataBuilder.CreatePublication(userId: userId),
                TestDataBuilder.CreatePublication(userId: userId)
            };

            _repositoryMock.Setup(r => r.GetByUserIdAsync(userId))
                .ReturnsAsync(publications);

            // Act
            var result = await _service.GetByUserIdAsync(userId);

            // Assert
            result.Should().HaveCount(3);
            result.Should().AllSatisfy(p => p.UserId.Should().Be(userId));
        }

        #endregion

        #region CreateAsync Tests

        [Fact]
        public async Task CreateAsync_ConDatosValidos_DeberiaCrearPublicacion()
        {
            // Arrange
            var dto = TestDataBuilder.CreatePublicationDto();
            var userId = "user123";

            _repositoryMock.Setup(r => r.CreateAsync(It.IsAny<Publication>()))
                .Returns(Task.CompletedTask);

            // Act
            await _service.CreateAsync(dto, userId);

            // Assert
            _repositoryMock.Verify(r => r.CreateAsync(
                It.Is<Publication>(p =>
                    p.Title == dto.Title &&
                    p.UserId == userId)),
                Times.Once);
        }

        [Fact]
        public async Task CreateAsync_ConMalasPalabrasEnTitulo_DeberiaLanzarExcepcion()
        {
            // Arrange
            var dto = new PublicationDto
            {
                Title = "Este puto título",
                Description = "Descripción normal",
                Location = "Ciudad",
                ImageUrl = "http://example.com/image.jpg",
                Latitud = "10.0",
                Longitud = "20.0"
            };
            var userId = "user123";

            // Act
            Func<Task> act = async () => await _service.CreateAsync(dto, userId);

            // Assert
            await act.Should().ThrowAsync<BadWordsException>()
                .Where(ex => ex.Field == "Título" && ex.BadWords.Contains("puto"));

            _repositoryMock.Verify(r => r.CreateAsync(It.IsAny<Publication>()), Times.Never);
        }

        [Fact]
        public async Task CreateAsync_ConMalasPalabrasEnDescripcion_DeberiaLanzarExcepcion()
        {
            // Arrange
            var dto = new PublicationDto
            {
                Title = "Título normal",
                Description = "Esta mierda de descripción",
                Location = "Ciudad",
                ImageUrl = "http://example.com/image.jpg",
                Latitud = "10.0",
                Longitud = "20.0"
            };
            var userId = "user123";

            // Act
            Func<Task> act = async () => await _service.CreateAsync(dto, userId);

            // Assert
            await act.Should().ThrowAsync<BadWordsException>()
                .Where(ex => ex.Field == "Descripción" && ex.BadWords.Contains("mierda"));
        }

        [Fact]
        public async Task CreateAsync_ConMalasPalabrasEnUbicacion_DeberiaLanzarExcepcion()
        {
            // Arrange
            var dto = new PublicationDto
            {
                Title = "Título normal",
                Description = "Descripción normal",
                Location = "Ciudad pendeja",
                ImageUrl = "http://example.com/image.jpg",
                Latitud = "10.0",
                Longitud = "20.0"
            };
            var userId = "user123";

            // Act
            Func<Task> act = async () => await _service.CreateAsync(dto, userId);

            // Assert
            await act.Should().ThrowAsync<BadWordsException>()
                .Where(ex => ex.Field == "Ubicación");
        }

        #endregion

        #region UpdateAsync Tests

        [Fact]
        public async Task UpdateAsync_ConDatosValidos_DeberiaActualizarPublicacion()
        {
            // Arrange
            var publication = TestDataBuilder.CreatePublication();
            var dto = new PublicationDto
            {
                Title = "Nuevo título",
                Description = "Nueva descripción",
                Location = "Nueva ubicación"
            };

            _repositoryMock.Setup(r => r.GetByIdAsync(publication.Id))
                .ReturnsAsync(publication);
            _repositoryMock.Setup(r => r.UpdateAsync(It.IsAny<Publication>()))
                .Returns(Task.CompletedTask);

            // Act
            var result = await _service.UpdateAsync(publication.Id, dto, publication.UserId);

            // Assert
            result.Should().BeTrue();
            _repositoryMock.Verify(r => r.UpdateAsync(
                It.Is<Publication>(p =>
                    p.Title == dto.Title &&
                    p.Description == dto.Description)),
                Times.Once);
        }

        [Fact]
        public async Task UpdateAsync_UsuarioNoEsPropietario_DeberiaRetornarFalse()
        {
            // Arrange
            var publication = TestDataBuilder.CreatePublication(userId: "owner123");
            var dto = TestDataBuilder.CreatePublicationDto();

            _repositoryMock.Setup(r => r.GetByIdAsync(publication.Id))
                .ReturnsAsync(publication);

            // Act
            var result = await _service.UpdateAsync(publication.Id, dto, "otherUser456");

            // Assert
            result.Should().BeFalse();
            _repositoryMock.Verify(r => r.UpdateAsync(It.IsAny<Publication>()), Times.Never);
        }

        [Fact]
        public async Task UpdateAsync_PublicacionNoExiste_DeberiaRetornarFalse()
        {
            // Arrange
            var dto = TestDataBuilder.CreatePublicationDto();
            _repositoryMock.Setup(r => r.GetByIdAsync(It.IsAny<string>()))
                .ReturnsAsync((Publication?)null);

            // Act
            var result = await _service.UpdateAsync("id-inexistente", dto, "user123");

            // Assert
            result.Should().BeFalse();
        }

        [Fact]
        public async Task UpdateAsync_ConMalasPalabrasEnTitulo_DeberiaLanzarExcepcion()
        {
            // Arrange
            var publication = TestDataBuilder.CreatePublication();
            var dto = new PublicationDto
            {
                Title = "Título con puto contenido"
            };

            _repositoryMock.Setup(r => r.GetByIdAsync(publication.Id))
                .ReturnsAsync(publication);

            // Act
            Func<Task> act = async () => await _service.UpdateAsync(
                publication.Id, dto, publication.UserId);

            // Assert
            await act.Should().ThrowAsync<BadWordsException>()
                .Where(ex => ex.Field == "Título");
        }

        #endregion

        #region DeleteAsync Tests

        [Fact]
        public async Task DeleteAsync_UsuarioPropietario_DeberiaEliminarPublicacion()
        {
            // Arrange
            var publication = TestDataBuilder.CreatePublication();
            _repositoryMock.Setup(r => r.GetByIdAsync(publication.Id))
                .ReturnsAsync(publication);
            _repositoryMock.Setup(r => r.DeleteAsync(publication.Id))
                .Returns(Task.CompletedTask);

            // Act
            var result = await _service.DeleteAsync(publication.Id, publication.UserId);

            // Assert
            result.Should().BeTrue();
            _repositoryMock.Verify(r => r.DeleteAsync(publication.Id), Times.Once);
        }

        [Fact]
        public async Task DeleteAsync_UsuarioNoEsPropietario_DeberiaRetornarFalse()
        {
            // Arrange
            var publication = TestDataBuilder.CreatePublication(userId: "owner123");
            _repositoryMock.Setup(r => r.GetByIdAsync(publication.Id))
                .ReturnsAsync(publication);

            // Act
            var result = await _service.DeleteAsync(publication.Id, "otherUser456");

            // Assert
            result.Should().BeFalse();
            _repositoryMock.Verify(r => r.DeleteAsync(It.IsAny<string>()), Times.Never);
        }

        [Fact]
        public async Task DeleteAdminAsync_PublicacionExiste_DeberiaEliminar()
        {
            // Arrange
            var publication = TestDataBuilder.CreatePublication();
            _repositoryMock.Setup(r => r.GetByIdAsync(publication.Id))
                .ReturnsAsync(publication);
            _repositoryMock.Setup(r => r.DeleteAsync(publication.Id))
                .Returns(Task.CompletedTask);

            // Act
            var result = await _service.DeleteAdminAsync(publication.Id);

            // Assert
            result.Should().BeTrue();
            _repositoryMock.Verify(r => r.DeleteAsync(publication.Id), Times.Once);
        }

        #endregion
    }
}