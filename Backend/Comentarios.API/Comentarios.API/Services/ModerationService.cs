using System.Text;
using System.Text.RegularExpressions;
using System.Globalization;
using Comentarios.API.DTOs;

namespace Comentarios.API.Services;

public interface IModerationService
{
    Task<ModerationResponse> AnalyzeAsync(string text);
}

public class ModerationService : IModerationService
{
    // Lista local y gratuita de palabras ofensivas (ampliada)
    private static readonly HashSet<string> OffensiveWords = new(StringComparer.OrdinalIgnoreCase)
    {
        "puta","puto","mierda","idiota","estupido","estúpido","imbecil","imbécil","tonto","pendejo",
        "terror","kill","asshole","bastard","fuck","shit","racist","racismo","feo","maldito","gilipollas",
        "cabron","cabrón","mamón","putona","zorra"
    };

    private static string NormalizeText(string input)
    {
        if (string.IsNullOrWhiteSpace(input)) return string.Empty;
        // Normalizar para eliminar tildes y marcar en minúsculas
        var normalized = input.Normalize(NormalizationForm.FormD);
        var sb = new StringBuilder();
        foreach (var ch in normalized)
        {
            var uc = CharUnicodeInfo.GetUnicodeCategory(ch);
            if (uc != UnicodeCategory.NonSpacingMark)
            {
                sb.Append(ch);
            }
        }
        return sb.ToString().Normalize(NormalizationForm.FormC).ToLowerInvariant();
    }

    public Task<ModerationResponse> AnalyzeAsync(string text)
    {
        if (string.IsNullOrWhiteSpace(text))
            return Task.FromResult(new ModerationResponse { IsOffensive = false, Flags = Array.Empty<string>(), Score = 0 });

        var normalized = NormalizeText(text);

        var words = Regex.Matches(normalized, @"\p{L}+")
                         .Select(m => m.Value)
                         .ToArray();

        var matches = words.Where(w => OffensiveWords.Contains(w))
                           .Distinct(StringComparer.OrdinalIgnoreCase)
                           .ToArray();

        var score = words.Length == 0 ? 0.0 : (double)matches.Length / words.Length;
        var isOffensive = matches.Length > 0 || score > 0.05; // umbral heurístico

        var reason = matches.Length > 0 ? "Coincidencia de palabras prohibidas" : (score > 0.05 ? "Alta densidad de términos" : "No ofensivo");

        return Task.FromResult(new ModerationResponse
        {
            IsOffensive = isOffensive,
            Flags = matches,
            Score = Math.Round(score, 3),
            Reason = reason
        });
    }
}