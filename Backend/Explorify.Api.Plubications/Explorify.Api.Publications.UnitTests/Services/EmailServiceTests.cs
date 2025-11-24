using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using Explorify.Api.Publications.Infraestructure.Services;
using Explorify.Api.Publications.UnitTests.Helpers;
using FluentAssertions;
using Moq;
using Xunit;

namespace Explorify.Api.Publications.UnitTests.Services
{
    public class EmailServiceTests
    {
        private readonly Mock<IEmailRepository> _emailRepositoryMock;
        private readonly Mock<IEmailVerificationService> _verificationServiceMock;
        private readonly EmailService _service;

        public EmailServiceTests()
        {
            _emailRepositoryMock = new Mock<IEmailRepository>();
            _verificationServiceMock = new Mock<IEmailVerificationService>();
            _service = new EmailService(
                _emailRepositoryMock.Object,
                _verificationServiceMock.Object);
        }

        #region SendEmailAsync Tests

        [Fact]
        public async Task SendEmailAsync_ConDatosValidos_DeberiaEnviarCorreo()
        {
            // Arrange
            var emailDto = TestDataBuilder.CreateEmailDto();

            _verificationServiceMock.Setup(v => v.VerifyEmail(emailDto.To))
                .ReturnsAsync(new EmailVerificationResponseDto
                {
                    Success = true,
                    Status = "valid",
                    SubStatus = ""
                });

            _emailRepositoryMock.Setup(r => r.SendEmailAsync(It.IsAny<Email>()))
                .ReturnsAsync((true, ""));

            // Act
            var result = await _service.SendEmailAsync(emailDto);

            // Assert
            result.IsSuccess.Should().BeTrue();
            result.Message.Should().Be("Correo enviado correctamente.");
            _emailRepositoryMock.Verify(r => r.SendEmailAsync(
                It.Is<Email>(e =>
                    e.To == emailDto.To &&
                    e.Subject == emailDto.Subject &&
                    e.Body == emailDto.Body)),
                Times.Once);
        }

        [Theory]
        [InlineData("", "Subject", "Body")]
        [InlineData("test@test.com", "", "Body")]
        [InlineData("test@test.com", "Subject", "")]
        [InlineData(null, "Subject", "Body")]
        public async Task SendEmailAsync_ConCamposVacios_DeberiaRetornarError(
            string to, string subject, string body)
        {
            // Arrange
            var emailDto = new EmailDto
            {
                To = to,
                Subject = subject,
                Body = body
            };

            // Act
            var result = await _service.SendEmailAsync(emailDto);

            // Assert
            result.IsSuccess.Should().BeFalse();
            result.Message.Should().Contain("proporcionar");
            _emailRepositoryMock.Verify(r => r.SendEmailAsync(
                It.IsAny<Email>()), Times.Never);
        }

        [Theory]
        [InlineData("correo-invalido")]
        [InlineData("@ejemplo.com")]
        [InlineData("correo@")]
        [InlineData("correo sin arroba.com")]
        public async Task SendEmailAsync_ConEmailInvalido_DeberiaRetornarError(string invalidEmail)
        {
            // Arrange
            var emailDto = new EmailDto
            {
                To = invalidEmail,
                Subject = "Test",
                Body = "Body"
            };

            // Act
            var result = await _service.SendEmailAsync(emailDto);

            // Assert
            result.IsSuccess.Should().BeFalse();
            result.Message.Should().Contain("no es válido");
            _emailRepositoryMock.Verify(r => r.SendEmailAsync(
                It.IsAny<Email>()), Times.Never);
        }

        [Fact]
        public async Task SendEmailAsync_EmailInvalidoSegunZeroBounce_DeberiaRetornarError()
        {
            // Arrange
            var emailDto = TestDataBuilder.CreateEmailDto();

            _verificationServiceMock.Setup(v => v.VerifyEmail(emailDto.To))
                .ReturnsAsync(new EmailVerificationResponseDto
                {
                    Success = true,
                    Status = "invalid",
                    SubStatus = "mailbox_not_found"
                });

            // Act
            var result = await _service.SendEmailAsync(emailDto);

            // Assert
            result.IsSuccess.Should().BeFalse();
            result.Message.Should().Contain("inválido según ZeroBounce");
            _emailRepositoryMock.Verify(r => r.SendEmailAsync(
                It.IsAny<Email>()), Times.Never);
        }

        [Fact]
        public async Task SendEmailAsync_EmailDesconocidoSegunZeroBounce_DeberiaRetornarError()
        {
            // Arrange
            var emailDto = TestDataBuilder.CreateEmailDto();

            _verificationServiceMock.Setup(v => v.VerifyEmail(emailDto.To))
                .ReturnsAsync(new EmailVerificationResponseDto
                {
                    Success = true,
                    Status = "unknow",
                    SubStatus = ""
                });

            // Act
            var result = await _service.SendEmailAsync(emailDto);

            // Assert
            result.IsSuccess.Should().BeFalse();
            result.Message.Should().Contain("inválido según ZeroBounce");
        }

        [Fact]
        public async Task SendEmailAsync_ErrorAlEnviar_DeberiaRetornarError()
        {
            // Arrange
            var emailDto = TestDataBuilder.CreateEmailDto();
            var errorMessage = "Error de conexión SMTP";

            _verificationServiceMock.Setup(v => v.VerifyEmail(emailDto.To))
                .ReturnsAsync(new EmailVerificationResponseDto
                {
                    Success = true,
                    Status = "valid",
                    SubStatus = ""
                });

            _emailRepositoryMock.Setup(r => r.SendEmailAsync(It.IsAny<Email>()))
                .ReturnsAsync((false, errorMessage));

            // Act
            var result = await _service.SendEmailAsync(emailDto);

            // Assert
            result.IsSuccess.Should().BeFalse();
            result.Message.Should().Be(errorMessage);
        }

        #endregion
    }
}