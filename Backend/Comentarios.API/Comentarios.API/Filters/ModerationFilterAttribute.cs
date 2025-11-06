using System.Reflection;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Comentarios.API.Services;
using Comentarios.API.DTOs;

namespace Comentarios.API.Filters;

public class ModerationFilterAttribute : IAsyncActionFilter
{
    private readonly IModerationService _moderationService;

    public ModerationFilterAttribute(IModerationService moderationService)
    {
        _moderationService = moderationService;
    }

    public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
    {
        // Buscar en los argumentos de la acción cualquier string directo o propiedad string llamada "Text"/"Content"
        string? textToAnalyze = null;

        foreach (var arg in context.ActionArguments.Values)
        {
            if (arg == null) continue;

            // Si el argumento es string puro
            if (arg is string s && !string.IsNullOrWhiteSpace(s))
            {
                textToAnalyze = s;
                break;
            }

            // Buscar propiedades habituales en modelos
            var type = arg.GetType();
            var prop = type.GetProperty("Text", BindingFlags.Public | BindingFlags.Instance | BindingFlags.IgnoreCase)
                       ?? type.GetProperty("Content", BindingFlags.Public | BindingFlags.Instance | BindingFlags.IgnoreCase)
                       ?? type.GetProperty("Comentario", BindingFlags.Public | BindingFlags.Instance | BindingFlags.IgnoreCase)
                       ?? type.GetProperty("Message", BindingFlags.Public | BindingFlags.Instance | BindingFlags.IgnoreCase);

            if (prop != null && prop.PropertyType == typeof(string))
            {
                var val = prop.GetValue(arg) as string;
                if (!string.IsNullOrWhiteSpace(val))
                {
                    textToAnalyze = val;
                    break;
                }
            }
        }

        if (!string.IsNullOrWhiteSpace(textToAnalyze))
        {
            var result = await _moderationService.AnalyzeAsync(textToAnalyze!);
            if (result.IsOffensive)
            {
                // Responder y cortar la ejecución si es ofensivo
                context.Result = new BadRequestObjectResult(new
                {
                    message = "Contenido ofensivo detectado.",
                    flags = result.Flags,
                    score = result.Score,
                    reason = result.Reason
                });
                return;
            }
        }

        await next();
    }
}