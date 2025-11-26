using Comentarios.API.Services;
using FluentAssertions;
using System.Globalization;
using System.Text;
using Xunit;

namespace Comentarios.API.Tests.Unit
{
    public class ModerationServiceTests
    {
        private readonly IModerationService _moderationService;

        public ModerationServiceTests()
        {
            _moderationService = new ModerationService();
        }

        [Fact]
        public async Task AnalyzeAsync_TextoLimpio_NoDebeSerOfensivo()
        {
            // Arrange
            var texto = "Este es un comentario muy bonito y respetuoso";

            // Act
            var resultado = await _moderationService.AnalyzeAsync(texto);

            // Assert
            resultado.Should().NotBeNull();
            resultado.IsOffensive.Should().BeFalse();
            resultado.Flags.Should().BeEmpty();
            resultado.Score.Should().Be(0);
        }

        [Fact]
        public async Task AnalyzeAsync_TextoConPalabraOfensiva_DebeDetectarla()
        {
            // Arrange
            var texto = "Eres un idiota";

            // Act
            var resultado = await _moderationService.AnalyzeAsync(texto);

            // Assert
            resultado.Should().NotBeNull();
            resultado.IsOffensive.Should().BeTrue();
            resultado.Flags.Should().Contain("idiota");
            resultado.Score.Should().BeGreaterThan(0);
        }

        [Fact]
        public async Task AnalyzeAsync_TextoConVariasPalabrasOfensivas_DebeDetectarlas()
        {
            // Arrange
            var texto = "Eres un idiota y un tonto";

            // Act
            var resultado = await _moderationService.AnalyzeAsync(texto);

            // Assert
            resultado.Should().NotBeNull();
            resultado.IsOffensive.Should().BeTrue();
            resultado.Flags.Length.Should().BeGreaterThanOrEqualTo(2);
            resultado.Score.Should().BeGreaterThan(0);
        }

        [Fact]
        public async Task AnalyzeAsync_TextoVacio_NoDebeSerOfensivo()
        {
            // Arrange
            var texto = "";

            // Act
            var resultado = await _moderationService.AnalyzeAsync(texto);

            // Assert
            resultado.Should().NotBeNull();
            resultado.IsOffensive.Should().BeFalse();
            resultado.Flags.Should().BeEmpty();
            resultado.Score.Should().Be(0);
        }

        [Fact]
        public async Task AnalyzeAsync_TextoConTildes_DebeNormalizar()
        {
            // Arrange
            var texto = "Eres un estúpido";

            // Act
            var resultado = await _moderationService.AnalyzeAsync(texto);

            // Assert
            resultado.Should().NotBeNull();
            resultado.IsOffensive.Should().BeTrue();
            resultado.Flags.Should().NotBeEmpty();
        }

        [Theory]
        [InlineData("puta")]
        [InlineData("mierda")]
        [InlineData("pendejo")]
        [InlineData("cabrón")]
        public async Task AnalyzeAsync_PalabrasOfensivasEspecificas_DebeDetectar(string palabraOfensiva)
        {
            // Arrange
            var texto = $"Este texto contiene {palabraOfensiva}";

            // Act
            var resultado = await _moderationService.AnalyzeAsync(texto);

            // Assert
            var normalizada = RemoverAcentos(palabraOfensiva.ToLowerInvariant());

            resultado.Should().NotBeNull();
            resultado.IsOffensive.Should().BeTrue();

            // Se valida contra la versión sin acento
            resultado.Flags.Should().Contain(normalizada);
        }

        [Fact]
        public async Task AnalyzeAsync_TextoLargo_DebeCalcularScoreCorrectamente()
        {
            // Arrange
            var texto = "Este es un comentario muy largo con contenido apropiado y respetuoso";

            // Act
            var resultado = await _moderationService.AnalyzeAsync(texto);

            // Assert
            resultado.Should().NotBeNull();
            resultado.Score.Should().BeGreaterThanOrEqualTo(0);
            resultado.Score.Should().BeLessThanOrEqualTo(1);
        }

        // Helper para normalizar como el servicio
        private static string RemoverAcentos(string texto)
        {
            return new string(
                texto
                    .Normalize(NormalizationForm.FormD)
                    .Where(c => CharUnicodeInfo.GetUnicodeCategory(c) != UnicodeCategory.NonSpacingMark)
                    .ToArray()
            ).Normalize(NormalizationForm.FormC);
        }
    }
}
