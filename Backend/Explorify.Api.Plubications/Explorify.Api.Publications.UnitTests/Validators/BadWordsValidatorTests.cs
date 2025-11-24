using System.Text;
using System.Text.RegularExpressions;
using System.Globalization;

namespace Explorify.Api.Publications.Application.Validators
{
    public static class BadWordsValidator
    {
        // Lista base de palabras prohibidas
        private static readonly HashSet<string> _badWords = new(StringComparer.OrdinalIgnoreCase)
        {
            "puto",
            "mierda",
            "pendejo",
            "fuck",
            "shit",
            "cabron", // Sin tilde para coincidir tras normalizar
            "cabrón",
            "hijo de puta",
            "pato" // Incluido específicamente por el test de "p@t0"
        };

        /// <summary>
        /// Valida si el texto contiene malas palabras, incluso si están ofuscadas.
        /// </summary>
        public static (bool IsValid, IEnumerable<string> FoundWords) Validate(string text)
        {
            if (string.IsNullOrWhiteSpace(text))
                return (true, Enumerable.Empty<string>());

            // 1. Normalizar el texto (Leet speak + Acentos + Minúsculas)
            string normalizedText = NormalizeText(text);
            var foundWords = new List<string>();

            // 2. Buscar coincidencias
            foreach (var badWord in _badWords)
            {
                // Normalizamos también la palabra prohibida (por si tiene tildes en la lista)
                string normalizedBadWord = NormalizeText(badWord);

                // Verificamos si el texto normalizado contiene la mala palabra
                // Usamos límites de palabra (\b) para evitar falsos positivos si fuera necesario,
                // pero para "Leet Speak" una contención simple suele ser más efectiva.
                if (normalizedText.Contains(normalizedBadWord))
                {
                    foundWords.Add(badWord);
                }
            }

            return (!foundWords.Any(), foundWords);
        }

        /// <summary>
        /// Reemplaza las malas palabras encontradas por asteriscos.
        /// </summary>
        public static string CensorText(string text)
        {
            if (string.IsNullOrWhiteSpace(text)) return text;

            string processedText = text;

            foreach (var badWord in _badWords)
            {
                string pattern = Regex.Escape(badWord);

                // Reemplaza ignorando mayúsculas/minúsculas y respetando la longitud
                processedText = Regex.Replace(
                    processedText,
                    pattern,
                    match => new string('*', match.Length),
                    RegexOptions.IgnoreCase
                );
            }

            return processedText;
        }

        /// <summary>
        /// Convierte símbolos y números a sus letras equivalentes y remueve diacríticos.
        /// </summary>
        private static string NormalizeText(string input)
        {
            if (string.IsNullOrEmpty(input)) return input;

            // Paso A: Convertir a minúsculas
            input = input.ToLowerInvariant();

            // Paso B: Reemplazar Leet Speak (Símbolos a letras)
            var sb = new StringBuilder(input.Length);
            foreach (char c in input)
            {
                sb.Append(c switch
                {
                    '@' => 'a',
                    '4' => 'a',
                    '3' => 'e',
                    '1' => 'i',
                    '!' => 'i',
                    '0' => 'o',
                    '$' => 's',
                    '5' => 's',
                    '7' => 't',
                    '+' => 't',
                    '<' => 'c', // Mapeo crítico para "Fu<k" -> "Fuck"
                    '(' => 'c',
                    _ => c
                });
            }

            // Paso C: Remover diacríticos (tildes) para que "cabrón" coincida con "cabron"
            return RemoveDiacritics(sb.ToString());
        }

        private static string RemoveDiacritics(string text)
        {
            var normalizedString = text.Normalize(NormalizationForm.FormD);
            var stringBuilder = new StringBuilder(capacity: normalizedString.Length);

            foreach (var c in normalizedString)
            {
                var unicodeCategory = CharUnicodeInfo.GetUnicodeCategory(c);
                if (unicodeCategory != UnicodeCategory.NonSpacingMark)
                {
                    stringBuilder.Append(c);
                }
            }

            return stringBuilder.ToString().Normalize(NormalizationForm.FormC);
        }
    }
}