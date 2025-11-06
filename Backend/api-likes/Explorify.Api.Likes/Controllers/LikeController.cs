using Explorify.Api.Likes.Application.Dtos;
using Explorify.Api.Likes.Application.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Explorify.Api.Likes.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class LikeController : ControllerBase
    {
        private readonly ILikeService _likeService;

        public LikeController(ILikeService likeService)
        {
            _likeService = likeService;
        }

        private string GetUserId()
        {
            return User.FindFirstValue(ClaimTypes.NameIdentifier)
                ?? throw new UnauthorizedAccessException("Usuario no autenticado");
        }

        /// <summary>
        /// Agregar o quitar like (toggle)
        /// </summary>
        [HttpPost("toggle")]
        public async Task<ActionResult<ResponseDto>> ToggleLike([FromBody] LikeRequestDto request)
        {
            try
            {
                var userId = GetUserId();
                var hasLiked = await _likeService.UserHasLikedAsync(request.PublicationId, userId);

                if (hasLiked)
                {
                    await _likeService.RemoveLikeAsync(request.PublicationId, userId);
                    return Ok(new ResponseDto
                    {
                        Result = new { Action = "removed" },
                        IsSuccess = true,
                        Message = "Like eliminado"
                    });
                }
                else
                {
                    var like = await _likeService.AddLikeAsync(request.PublicationId, userId);
                    return Ok(new ResponseDto
                    {
                        Result = like,
                        IsSuccess = true,
                        Message = "Like agregado"
                    });
                }
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

        /// <summary>
        /// Obtener estadísticas de likes de una publicación
        /// </summary>
        [HttpGet("stats/{publicationId}")]
        [AllowAnonymous]
        public async Task<ActionResult<ResponseDto>> GetLikeStats(string publicationId)
        {
            try
            {
                var userId = User.Identity?.IsAuthenticated == true
                    ? GetUserId()
                    : string.Empty;

                var stats = await _likeService.GetLikeStatsAsync(publicationId, userId);

                return Ok(new ResponseDto
                {
                    Result = stats,
                    IsSuccess = true,
                    Message = "Estadísticas obtenidas"
                });
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

        /// <summary>
        /// Obtener todos los likes de una publicación
        /// </summary>
        [HttpGet("publication/{publicationId}")]
        [AllowAnonymous]
        public async Task<ActionResult<ResponseDto>> GetLikesByPublication(string publicationId)
        {
            try
            {
                var likes = await _likeService.GetLikesByPublicationAsync(publicationId);

                return Ok(new ResponseDto
                {
                    Result = likes,
                    IsSuccess = true,
                    Message = "Likes obtenidos"
                });
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

        /// <summary>
        /// Obtener todos los likes de un usuario
        /// </summary>
        [HttpGet("user/{userId}")]
        public async Task<ActionResult<ResponseDto>> GetLikesByUser(string userId)
        {
            try
            {
                var likes = await _likeService.GetLikesByUserAsync(userId);

                return Ok(new ResponseDto
                {
                    Result = likes,
                    IsSuccess = true,
                    Message = "Likes del usuario obtenidos"
                });
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

        /// <summary>
        /// Verificar si el usuario dio like a una publicación
        /// </summary>
        [HttpGet("has-liked/{publicationId}")]
        public async Task<ActionResult<ResponseDto>> HasLiked(string publicationId)
        {
            try
            {
                var userId = GetUserId();
                var hasLiked = await _likeService.UserHasLikedAsync(publicationId, userId);

                return Ok(new ResponseDto
                {
                    Result = new { HasLiked = hasLiked },
                    IsSuccess = true,
                    Message = "Estado verificado"
                });
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
    }
}