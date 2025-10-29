using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;

namespace Explorify.Api.Publications.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class MediaController : ControllerBase
    {
        private readonly ICloudinaryService _cloudinaryService;

        public MediaController(ICloudinaryService cloudinaryService)
        {
            _cloudinaryService = cloudinaryService;
        }

        [HttpPost("upload/image")]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> UploadImage(IFormFile file, [FromForm] string? folder = null)
        {
            try
            {
                var result = await _cloudinaryService.UploadImageAsync(file, folder);
                return Ok(new ResponseDto { Result = result, Message = "Imagen subida exitosamente" });
            }
            catch (Exception ex)
            {
                return BadRequest(new ResponseDto
                {
                    IsSuccess = false,
                    Message = ex.Message
                });
            }
        }

        [HttpPost("upload/video")]
        [Consumes("multipart/form-data")] // <-- Buena práctica
        public async Task<IActionResult> UploadVideo(IFormFile file, [FromForm] string? folder = null) 
        {
            try
            {
                var result = await _cloudinaryService.UploadVideoAsync(file, folder);
                return Ok(new ResponseDto { Result = result, Message = "Video subido exitosamente" });
            }
            catch (Exception ex)
            {
                return BadRequest(new ResponseDto
                {
                    IsSuccess = false,
                    Message = ex.Message
                });
            }
        }

        [HttpDelete("{publicId}")]
        public async Task<IActionResult> DeleteMedia(string publicId)
        {
            var deleted = await _cloudinaryService.DeleteMediaAsync(publicId);

            if (!deleted)
                return NotFound(new ResponseDto
                {
                    IsSuccess = false,
                    Message = "Media no encontrado"
                });

            return Ok(new ResponseDto { Message = "Media eliminado correctamente" });
        }

        [HttpGet("optimize/{publicId}")]
        public IActionResult GetOptimizedUrl(string publicId, [FromQuery] int width = 800, [FromQuery] int height = 600)
        {
            var url = _cloudinaryService.GetOptimizedUrl(publicId, width, height);
            return Ok(new ResponseDto { Result = new { url } });
        }
    }
}