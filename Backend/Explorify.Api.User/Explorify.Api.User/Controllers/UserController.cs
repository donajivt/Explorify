using Explorify.Api.User.Application.Dtos;
using Explorify.Api.User.Application.Interfaces;
using Explorify.Api.User.Application.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;

namespace Explorify.Api.User.Api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class UsersController : ControllerBase
    {
        private readonly IUserService _userService;

        public UsersController(IUserService userService)
        {
            _userService = userService;
        }

        //private string GetUserIdFromToken()
        //{
        //    return User.FindFirstValue(ClaimTypes.NameIdentifier) ?? string.Empty;
        //}
        private string GetUserIdFromToken()
        {
            // Buscar primero en el claim "sub"
            var subClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub)?.Value;
            if (!string.IsNullOrEmpty(subClaim))
                return subClaim;

            return User.FindFirstValue(ClaimTypes.NameIdentifier) ?? string.Empty;
        }

        // Obtener todos los usuarios (solo accesible por administrador si lo deseas)
        [HttpGet]
        [Authorize]
        public async Task<IActionResult> GetAllUsers()
        {
            var response = await _userService.GetAllUsersAsync();
            return Ok(response);
        }

        // Obtener usuario por Id
        [HttpGet("{id:length(24)}")]
        [AllowAnonymous]
        public async Task<IActionResult> GetUserById(string id)
        {
            var response = await _userService.GetUserByIdAsync(id);
            return Ok(response);
        }

        [HttpGet("me")]
        public async Task<IActionResult> GetProfile()
        {
            var userId = GetUserIdFromToken();
            var response = await _userService.GetProfileAsync(userId);
            return Ok(response);
        }

        [HttpPut("me")]
        public async Task<IActionResult> UpdateProfile([FromBody] UserUpdateDto dto)
        {
            var userId = GetUserIdFromToken();
            var response = await _userService.UpdateProfileAsync(userId, dto);
            return Ok(response);
        }

        [HttpDelete("me")]
        public async Task<IActionResult> DeleteProfile()
        {
            var userId = GetUserIdFromToken();
            var response = await _userService.DeleteProfileAsync(userId);
            return Ok(response);
        }
    }
}
