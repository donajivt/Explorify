using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Exceptions;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Text.Json;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Infraestructure.Middleware
{
    /// <summary>
    /// Manejador global de excepciones
    /// </summary>
    public class GlobalExceptionHandler : IExceptionHandler
    {
        public async ValueTask<bool> TryHandleAsync(
            HttpContext httpContext,
            Exception exception,
            System.Threading.CancellationToken cancellationToken)
        {
            var response = httpContext.Response;
            response.ContentType = "application/json";

            ResponseDto responseDto;
            int statusCode;

            switch (exception)
            {
                case BadWordsException badWordsEx:
                    //  Respuesta específica para malas palabras
                    statusCode = StatusCodes.Status400BadRequest;
                    responseDto = new ResponseDto
                    {
                        IsSuccess = false,
                        Message = $"El campo '{badWordsEx.Field}' contiene palabras inapropiadas",
                        Result = new
                        {
                            field = badWordsEx.Field,
                            badWords = badWordsEx.BadWords,
                            errorType = "BadWordsDetected"
                        }
                    };
                    break;

                case ArgumentException argEx:
                    statusCode = StatusCodes.Status400BadRequest;
                    responseDto = new ResponseDto
                    {
                        IsSuccess = false,
                        Message = argEx.Message,
                        Result = new
                        {
                            errorType = "ValidationError"
                        }
                    };
                    break;

                case UnauthorizedAccessException:
                    statusCode = StatusCodes.Status401Unauthorized;
                    responseDto = new ResponseDto
                    {
                        IsSuccess = false,
                        Message = "No autorizado para realizar esta acción",
                        Result = new
                        {
                            errorType = "Unauthorized"
                        }
                    };
                    break;

                case KeyNotFoundException notFoundEx:
                    statusCode = StatusCodes.Status404NotFound;
                    responseDto = new ResponseDto
                    {
                        IsSuccess = false,
                        Message = notFoundEx.Message,
                        Result = new
                        {
                            errorType = "NotFound"
                        }
                    };
                    break;

                default:
                    // Error genérico del servidor
                    statusCode = StatusCodes.Status500InternalServerError;
                    responseDto = new ResponseDto
                    {
                        IsSuccess = false,
                        Message = "Ha ocurrido un error interno en el servidor",
                        Result = new
                        {
                            errorType = "InternalServerError",
                            // Solo en desarrollo mostrar el detalle
                            details = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") == "Development"
                                ? exception.Message
                                : null
                        }
                    };
                    break;
            }

            response.StatusCode = statusCode;

            var options = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            };

            await response.WriteAsync(
                JsonSerializer.Serialize(responseDto, options),
                cancellationToken);

            return true;
        }
    }
}