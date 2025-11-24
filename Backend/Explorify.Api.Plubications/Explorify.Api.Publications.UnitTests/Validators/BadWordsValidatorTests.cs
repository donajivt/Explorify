using Explorify.Api.Publications.Application.Validators;
using FluentAssertions;
using Xunit;

namespace Explorify.Api.Publications.UnitTests.Validators
{
    public class BadWordsValidatorTests
    {
        [Theory]
        [InlineData("Este es un texto limpio", true)]
        [InlineData("Un hermoso lugar para visitar", true)]
        [InlineData("Recomiendo este sitio turístico", true)]
        [InlineData("", true)]
        [InlineData(null, true)]
        public void Validate_TextoLimpio_DeberiaRetornarValido(string texto, bool expectedValid)
        {
            // Act
            var (isValid, foundWords) = BadWordsValidator.Validate(texto);

            // Assert
            isValid.Should().Be(expectedValid);
            foundWords.Should().BeEmpty();
        }

        [Theory]
        [InlineData("Este texto contiene puto", false, "puto")]
        [InlineData("Qué mierda de lugar", false, "mierda")]
        [InlineData("Este pendejo no sabe nada", false, "pendejo")]
        [InlineData("What a fuck place", false, "fuck")]
        [InlineData("This shit is terrible", false, "shit")]
        public void Validate_TextoConMalasPalabras_DeberiaRetornarInvalido(
            string texto,
            bool expectedValid,
            string expectedBadWord)
        {
            // Act
            var (isValid, foundWords) = BadWordsValidator.Validate(texto);

            // Assert
            isValid.Should().Be(expectedValid);
            foundWords.Should().Contain(expectedBadWord);
            foundWords.Should().NotBeEmpty();
        }

        [Theory]
        [InlineData("Es un p@t0 lugar", "pato")]
        [InlineData("Qu3 m13rd4", "mierda")]
        [InlineData("Fu<k this", "fuck")]
        [InlineData("Sh!t happens", "shit")]
        public void Validate_TextoConCaracteresEspeciales_DeberiaDetectarMalasPalabras(
            string texto,
            string expectedCleanWord)
        {
            // Act
            var (isValid, foundWords) = BadWordsValidator.Validate(texto);

            // Assert
            isValid.Should().BeFalse();
            foundWords.Should().Contain(w => w.ToLower().Contains(expectedCleanWord.Substring(0, 3)));
        }

        [Fact]
        public void Validate_TextoConVariasMalasPalabras_DeberiaRetornarTodas()
        {
            // Arrange
            var texto = "Este puto lugar está de mierda";

            // Act
            var (isValid, foundWords) = BadWordsValidator.Validate(texto);

            // Assert
            isValid.Should().BeFalse();
            foundWords.Should().HaveCountGreaterOrEqualTo(2);
            foundWords.Should().Contain("puto");
            foundWords.Should().Contain("mierda");
        }

        [Theory]
        [InlineData("Este texto contiene puto", "Este texto contiene ****")]
        [InlineData("Qué mierda de lugar", "Qué ****** de lugar")]
        [InlineData("Texto limpio", "Texto limpio")]
        public void CensorText_DeberiaCensurarMalasPalabras(string input, string expected)
        {
            // Act
            var result = BadWordsValidator.CensorText(input);

            // Assert
            result.Should().Be(expected);
        }

        [Theory]
        [InlineData("Cabrón con acento")]
        [InlineData("PUTO en mayúsculas")]
        [InlineData("MiErDa en mezcla")]
        public void Validate_DiferentesFormatos_DeberiaDetectarMalasPalabras(string texto)
        {
            // Act
            var (isValid, foundWords) = BadWordsValidator.Validate(texto);

            // Assert
            isValid.Should().BeFalse();
            foundWords.Should().NotBeEmpty();
        }

        [Fact]
        public void Validate_FraseCompletaOfensiva_DeberiaDetectar()
        {
            // Arrange
            var texto = "Eres un hijo de puta";

            // Act
            var (isValid, foundWords) = BadWordsValidator.Validate(texto);

            // Assert
            isValid.Should().BeFalse();
            foundWords.Should().Contain("hijo de puta");
        }
    }
}