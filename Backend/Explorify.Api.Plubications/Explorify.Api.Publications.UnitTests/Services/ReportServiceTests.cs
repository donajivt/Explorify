using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using Explorify.Api.Publications.Domain.Interfaces;
using Explorify.Api.Publications.Infraestructure.Services;
using Explorify.Api.Publications.UnitTests.Helpers;
using FluentAssertions;
using Moq;
using Xunit;

namespace Explorify.Api.Publications.UnitTests.Services
{
    public class ReportServiceTests
    {
        private readonly Mock<IReportRepository> _repositoryMock;
        private readonly ReportService _service;

        public ReportServiceTests()
        {
            _repositoryMock = new Mock<IReportRepository>();
            _service = new ReportService(_repositoryMock.Object);
        }

        #region CreateReportAsync Tests

        [Fact]
        public async Task CreateReportAsync_ConDatosValidos_DeberiaCrearReporte()
        {
            // Arrange
            var publicationId = "pub123";
            var userId = "user123";
            var reason = "Spam";
            var description = "Este es spam";

            _repositoryMock.Setup(r => r.CreateReportAsync(It.IsAny<PublicationReport>()))
                .Returns(Task.CompletedTask);

            // Act
            await _service.CreateReportAsync(publicationId, userId, reason, description);

            // Assert
            _repositoryMock.Verify(r => r.CreateReportAsync(
                It.Is<PublicationReport>(report =>
                    report.PublicationId == publicationId &&
                    report.ReportedByUserId == userId &&
                    report.Reason == reason &&
                    report.Description == description)),
                Times.Once);
        }

        [Fact]
        public async Task CreateReportAsync_SinDescripcion_DeberiaCrearReporte()
        {
            // Arrange
            var publicationId = "pub123";
            var userId = "user123";
            var reason = "Contenido inapropiado";

            _repositoryMock.Setup(r => r.CreateReportAsync(It.IsAny<PublicationReport>()))
                .Returns(Task.CompletedTask);

            // Act
            await _service.CreateReportAsync(publicationId, userId, reason, null);

            // Assert
            _repositoryMock.Verify(r => r.CreateReportAsync(
                It.Is<PublicationReport>(report =>
                    report.Description == string.Empty)),
                Times.Once);
        }

        [Theory]
        [InlineData("", "user123", "Reason")]
        [InlineData(null, "user123", "Reason")]
        [InlineData("  ", "user123", "Reason")]
        public async Task CreateReportAsync_PublicationIdInvalido_DeberiaLanzarExcepcion(
            string? publicationId, string userId, string reason)
        {
            // Act
            Func<Task> act = async () => await _service.CreateReportAsync(
                publicationId, userId, reason, null);

            // Assert
            await act.Should().ThrowAsync<ArgumentException>()
                .WithMessage("*publicationId*");

            _repositoryMock.Verify(r => r.CreateReportAsync(
                It.IsAny<PublicationReport>()), Times.Never);
        }

        [Theory]
        [InlineData("pub123", "", "Reason")]
        [InlineData("pub123", null, "Reason")]
        [InlineData("pub123", "  ", "Reason")]
        public async Task CreateReportAsync_UserIdInvalido_DeberiaLanzarExcepcion(
            string publicationId, string? userId, string reason)
        {
            // Act
            Func<Task> act = async () => await _service.CreateReportAsync(
                publicationId, userId, reason, null);

            // Assert
            await act.Should().ThrowAsync<ArgumentException>()
                .WithMessage("*reportedByUserId*");
        }

        [Theory]
        [InlineData("pub123", "user123", "")]
        [InlineData("pub123", "user123", null)]
        [InlineData("pub123", "user123", "  ")]
        public async Task CreateReportAsync_ReasonInvalido_DeberiaLanzarExcepcion(
            string publicationId, string userId, string? reason)
        {
            // Act
            Func<Task> act = async () => await _service.CreateReportAsync(
                publicationId, userId, reason, null);

            // Assert
            await act.Should().ThrowAsync<ArgumentException>()
                .WithMessage("*reason*");
        }

        #endregion

        #region GetAllReportsAsync Tests

        [Fact]
        public async Task GetAllReportsAsync_DeberiaRetornarTodosLosReportes()
        {
            // Arrange
            var reports = new List<PublicationReport>
            {
                TestDataBuilder.CreateReport(),
                TestDataBuilder.CreateReport(),
                TestDataBuilder.CreateReport()
            };

            _repositoryMock.Setup(r => r.GetAllReportsAsync())
                .ReturnsAsync(reports);

            // Act
            var result = await _service.GetAllReportsAsync();

            // Assert
            result.Should().HaveCount(3);
            result.Should().BeEquivalentTo(reports);
        }

        [Fact]
        public async Task GetAllReportsAsync_SinReportes_DeberiaRetornarListaVacia()
        {
            // Arrange
            _repositoryMock.Setup(r => r.GetAllReportsAsync())
                .ReturnsAsync(new List<PublicationReport>());

            // Act
            var result = await _service.GetAllReportsAsync();

            // Assert
            result.Should().BeEmpty();
        }

        #endregion

        #region GetReportByIdAsync Tests

        [Fact]
        public async Task GetReportByIdAsync_ConIdValido_DeberiaRetornarReporte()
        {
            // Arrange
            var report = TestDataBuilder.CreateReport();
            _repositoryMock.Setup(r => r.GetReportByIdAsync(report.Id))
                .ReturnsAsync(report);

            // Act
            var result = await _service.GetReportByIdAsync(report.Id);

            // Assert
            result.Should().NotBeNull();
            result.Should().BeEquivalentTo(report);
        }

        [Fact]
        public async Task GetReportByIdAsync_ConIdInexistente_DeberiaRetornarNull()
        {
            // Arrange
            _repositoryMock.Setup(r => r.GetReportByIdAsync(It.IsAny<string>()))
                .ReturnsAsync((PublicationReport?)null);

            // Act
            var result = await _service.GetReportByIdAsync("id-inexistente");

            // Assert
            result.Should().BeNull();
        }

        [Theory]
        [InlineData("")]
        [InlineData(null)]
        [InlineData("  ")]
        public async Task GetReportByIdAsync_ConIdInvalido_DeberiaLanzarExcepcion(string? id)
        {
            // Act
            Func<Task> act = async () => await _service.GetReportByIdAsync(id);

            // Assert
            await act.Should().ThrowAsync<ArgumentException>()
                .WithMessage("*id*");
        }

        #endregion

        #region GetReportsByPublicationIdAsync Tests

        [Fact]
        public async Task GetReportsByPublicationIdAsync_DeberiaRetornarReportesDePublicacion()
        {
            // Arrange
            var publicationId = "pub123";
            var reports = new List<PublicationReport>
            {
                TestDataBuilder.CreateReport(publicationId: publicationId),
                TestDataBuilder.CreateReport(publicationId: publicationId)
            };

            _repositoryMock.Setup(r => r.GetReportsByPublicationIdAsync(publicationId))
                .ReturnsAsync(reports);

            // Act
            var result = await _service.GetReportsByPublicationIdAsync(publicationId);

            // Assert
            result.Should().HaveCount(2);
            result.Should().AllSatisfy(r => r.PublicationId.Should().Be(publicationId));
        }

        [Theory]
        [InlineData("")]
        [InlineData(null)]
        [InlineData("  ")]
        public async Task GetReportsByPublicationIdAsync_ConIdInvalido_DeberiaLanzarExcepcion(
            string? publicationId)
        {
            // Act
            Func<Task> act = async () => await _service.GetReportsByPublicationIdAsync(publicationId);

            // Assert
            await act.Should().ThrowAsync<ArgumentException>()
                .WithMessage("*publicationId*");
        }

        #endregion

        #region GetReportsByUserIdAsync Tests

        [Fact]
        public async Task GetReportsByUserIdAsync_DeberiaRetornarReportesDelUsuario()
        {
            // Arrange
            var userId = "user123";
            var reports = new List<PublicationReport>
            {
                TestDataBuilder.CreateReport(reportedByUserId: userId),
                TestDataBuilder.CreateReport(reportedByUserId: userId),
                TestDataBuilder.CreateReport(reportedByUserId: userId)
            };

            _repositoryMock.Setup(r => r.GetReportsByUserIdAsync(userId))
                .ReturnsAsync(reports);

            // Act
            var result = await _service.GetReportsByUserIdAsync(userId);

            // Assert
            result.Should().HaveCount(3);
            result.Should().AllSatisfy(r => r.ReportedByUserId.Should().Be(userId));
        }

        [Theory]
        [InlineData("")]
        [InlineData(null)]
        [InlineData("  ")]
        public async Task GetReportsByUserIdAsync_ConIdInvalido_DeberiaLanzarExcepcion(string? userId)
        {
            // Act
            Func<Task> act = async () => await _service.GetReportsByUserIdAsync(userId);

            // Assert
            await act.Should().ThrowAsync<ArgumentException>()
                .WithMessage("*userId*");
        }

        #endregion
    }
}