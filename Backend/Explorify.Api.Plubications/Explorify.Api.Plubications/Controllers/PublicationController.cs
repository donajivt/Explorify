using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Infraestructure.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Explorify.Api.Plubications.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class PublicationController : ControllerBase
    {
        private readonly IPublicationService _service;
        private readonly IReportService _reportService;

        public PublicationController(IPublicationService service, IReportService reportService)
        {
            _service = service;
            _reportService = reportService;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var result = await _service.GetAllAsync();
            return Ok(new ResponseDto { Result = result });
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(string id)
        {
            var result = await _service.GetByIdAsync(id);
            if (result == null)
                return NotFound(new ResponseDto { IsSuccess = false, Message = "Publicación no encontrada" });

            return Ok(new ResponseDto { Result = result });
        }

        [HttpGet("location/{location}")]
        public async Task<IActionResult> GetByLocation(string location)
        {
            var result = await _service.GetByLocationAsync(location);
            if (result == null)
                return NotFound(new ResponseDto { IsSuccess = false, Message = "No hay publicaciones en esa ubicación." });

            return Ok(new ResponseDto { Result = result });
        }

        [HttpGet("user/{userId}")]
        public async Task<IActionResult> GetByUserId(string userId)
        {
            var result = await _service.GetByUserIdAsync(userId);
            if (result == null)
                return NotFound(new ResponseDto { IsSuccess = false, Message = "El usuario no cuenta con publicaciones." });
            return Ok(new ResponseDto { Result = result });
        }

        /// <summary>
        /// Crea una nueva publicación
        /// </summary>
        /// <remarks>
        /// Validaciones automáticas:
        /// - Detecta y rechaza malas palabras en título, descripción y ubicación
        /// - Valida que el usuario esté autenticado
        /// 
        /// Ejemplo de respuesta con error de malas palabras:
        /// 
        ///     {
        ///       "result": {
        ///         "field": "Descripción",
        ///         "badWords": ["mierda", "puto"],
        ///         "errorType": "BadWordsDetected"
        ///       },
        ///       "isSuccess": false,
        ///       "message": "El campo 'Descripción' contiene palabras inapropiadas"
        ///     }
        /// </remarks>
        [HttpPost]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public async Task<IActionResult> Create(PublicationDto dto)
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (userId == null)
                return Unauthorized(new ResponseDto { IsSuccess = false, Message = "Usuario no autenticado" });

            // ✅ Las excepciones de malas palabras serán manejadas por GlobalExceptionHandler
            await _service.CreateAsync(dto, userId);
            return Ok(new ResponseDto { Message = "Publicación creada exitosamente" });
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(string id)
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (userId == null)
                return Unauthorized(new ResponseDto { IsSuccess = false, Message = "Usuario no autenticado" });

            var deleted = await _service.DeleteAsync(id, userId);
            if (!deleted)
                return Forbid();

            return Ok(new ResponseDto { Message = "Publicación eliminada correctamente" });
        }

        /// <summary>
        /// Actualiza una publicación existente
        /// </summary>
        /// <remarks>
        /// Validaciones automáticas:
        /// - Detecta y rechaza malas palabras en campos actualizados
        /// - Verifica que el usuario sea el dueño de la publicación
        /// </remarks>
        [HttpPut("{id}")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        public async Task<IActionResult> Update(string id, PublicationDto dto)
        {
            var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (userId == null)
                return Unauthorized(new ResponseDto { IsSuccess = false, Message = "Usuario no autenticado" });

            // ✅ Las excepciones de malas palabras serán manejadas por GlobalExceptionHandler
            var updated = await _service.UpdateAsync(id, dto, userId);
            if (!updated)
                return Forbid();

            return Ok(new ResponseDto { Message = "Publicación actualizada correctamente" });
        }

        /// <summary>
        /// Crea un nuevo reporte sobre una publicación.
        /// </summary>
        [HttpPost("report")]
        public async Task<IActionResult> CreateReport([FromBody] ReportPublicationRequestDto dto)
        {
            if (string.IsNullOrEmpty(dto.PublicationId) || string.IsNullOrEmpty(dto.ReportedByUserId))
                return BadRequest("El ID de la publicación y del usuario son obligatorios.");

            await _reportService.CreateReportAsync(dto.PublicationId, dto.ReportedByUserId, dto.Reason, dto.Description);
            return Ok(new { message = "Reporte creado correctamente" });
        }

        /// <summary>
        /// Obtiene todos los reportes registrados.
        /// </summary>
        [HttpGet("report")]
        [Authorize(Roles = "Admin, ADMIN, admin")]
        public async Task<IActionResult> GetAllReports()
        {
            var reports = await _reportService.GetAllReportsAsync();
            return Ok(reports);
        }

        /// <summary>
        /// Obtiene un reporte por su ID.
        /// </summary>
        [HttpGet("report/{id}")]
        public async Task<IActionResult> GetReportById(string id)
        {
            var report = await _reportService.GetReportByIdAsync(id);
            if (report == null)
                return NotFound(new { message = "Reporte no encontrado" });

            return Ok(report);
        }

        /// <summary>
        /// Obtiene los reportes asociados a una publicación.
        /// </summary>
        [HttpGet("report/publication/{publicationId}")]
        public async Task<IActionResult> GetReportsByPublication(string publicationId)
        {
            var reports = await _reportService.GetReportsByPublicationIdAsync(publicationId);
            return Ok(reports);
        }

        /// <summary>
        /// Obtiene los reportes creados por un usuario.
        /// </summary>
        [HttpGet("report/user/{userId}")]
        public async Task<IActionResult> GetReportsByUser(string userId)
        {
            var reports = await _reportService.GetReportsByUserIdAsync(userId);
            return Ok(reports);
        }
    }
}