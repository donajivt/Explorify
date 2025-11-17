using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace Explorify.Api.Publications.Application.Validators
{
    public class BadWordsValidator
    {
        private static readonly HashSet<string> BadWords = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
        {
            // Palabras ofensivas en español
            "puto", "puta", "pendejo", "pendeja", "cabron", "cabrón", "verga", "chingado",
            "chingada", "pinche", "mamada", "cojones", "coño", "mierda", "joder",
            "imbécil", "idiota", "estúpido", "estúpida", "gilipollas", "marica",
            "maricón", "culero", "culera", "perra", "zorra", "hijo de puta",
            "hija de puta", "maldito", "maldita", "bastardo", "bastarda",
            
            // Palabras ofensivas en inglés
            "fuck", "shit", "bitch", "asshole", "damn", "crap", "dick", "cock",
            "pussy", "bastard", "motherfucker", "nigger", "nigga", "fag", "faggot",
            "retard", "whore", "slut", "cunt", "piss"
        };

        // Palabras compuestas o frases
        private static readonly List<string> BadPhrases = new List<string>
        {
            "hijo de puta",
            "hija de puta",
            "vete a la mierda",
            "vete al carajo",
            "me cago en",
            "mother fucker",
            "son of a bitch"
        };

        /// <summary>
        /// Valida si un texto contiene malas palabras
        /// </summary>
        /// <param name="text">Texto a validar</param>
        /// <returns>Tuple con (esValido, palabrasEncontradas)</returns>
        public static (bool IsValid, List<string> FoundWords) Validate(string text)
        {
            if (string.IsNullOrWhiteSpace(text))
                return (true, new List<string>());

            var foundWords = new List<string>();
            var normalizedText = NormalizeText(text);

            // Verificar frases completas primero
            foreach (var phrase in BadPhrases)
            {
                if (normalizedText.Contains(NormalizeText(phrase)))
                {
                    foundWords.Add(phrase);
                }
            }

            // Verificar palabras individuales
            var words = Regex.Split(normalizedText, @"\W+");
            foreach (var word in words)
            {
                if (string.IsNullOrWhiteSpace(word))
                    continue;

                // Verificar palabra exacta
                if (BadWords.Contains(word))
                {
                    if (!foundWords.Contains(word))
                        foundWords.Add(word);
                }

                // Verificar variaciones con caracteres especiales (ej: p@t0, pu+a)
                var cleanWord = RemoveSpecialCharacters(word);
                if (BadWords.Contains(cleanWord))
                {
                    if (!foundWords.Contains(cleanWord))
                        foundWords.Add(cleanWord);
                }
            }

            return (foundWords.Count == 0, foundWords);
        }

        /// <summary>
        /// Normaliza el texto removiendo acentos y convirtiéndolo a minúsculas
        /// </summary>
        private static string NormalizeText(string text)
        {
            if (string.IsNullOrWhiteSpace(text))
                return string.Empty;

            // Remover acentos
            var normalized = text.Normalize(System.Text.NormalizationForm.FormD);
            var result = new System.Text.StringBuilder();

            foreach (var c in normalized)
            {
                var category = System.Globalization.CharUnicodeInfo.GetUnicodeCategory(c);
                if (category != System.Globalization.UnicodeCategory.NonSpacingMark)
                {
                    result.Append(c);
                }
            }

            return result.ToString().ToLower();
        }

        /// <summary>
        /// Remueve caracteres especiales usados para evadir el filtro
        /// </summary>
        private static string RemoveSpecialCharacters(string text)
        {
            if (string.IsNullOrWhiteSpace(text))
                return string.Empty;

            // Reemplazos comunes para evadir filtros
            var replacements = new Dictionary<string, string>
            {
                { "@", "a" },
                { "4", "a" },
                { "3", "e" },
                { "1", "i" },
                { "!", "i" },
                { "0", "o" },
                { "5", "s" },
                { "$", "s" },
                { "+", "t" },
                { "7", "t" },
                { "*", "" },
                { "#", "" }
            };

            var result = text.ToLower();
            foreach (var replacement in replacements)
            {
                result = result.Replace(replacement.Key, replacement.Value);
            }

            return result;
        }

        /// <summary>
        /// Censura las malas palabras encontradas en un texto
        /// </summary>
        /// <param name="text">Texto a censurar</param>
        /// <returns>Texto censurado</returns>
        public static string CensorText(string text)
        {
            if (string.IsNullOrWhiteSpace(text))
                return text;

            var result = text;
            var (_, foundWords) = Validate(text);

            foreach (var badWord in foundWords)
            {
                var pattern = $@"\b{Regex.Escape(badWord)}\b";
                var replacement = new string('*', badWord.Length);
                result = Regex.Replace(result, pattern, replacement, RegexOptions.IgnoreCase);
            }

            return result;
        }
    }
}