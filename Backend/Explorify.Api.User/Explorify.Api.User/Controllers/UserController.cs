using Explorify.Api.User.Application.Dtos;
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
            var subClaim = User.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub)?.Value;
            if (!string.IsNullOrEmpty(subClaim))
                return subClaim;

            return User.FindFirstValue(ClaimTypes.NameIdentifier) ?? string.Empty;
        }

        [HttpGet]
        public async Task<IActionResult> GetAllUsers()
        {
            var response = await _userService.GetAllUsersAsync();
            return Ok(response);
        }

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
        public async Task<IActionResult> UpdateProfile([FromForm] UserUpdateDto dto, IFormFile? profileImage)
        {
            var userId = GetUserIdFromToken();
            var response = await _userService.UpdateProfileAsync(userId, dto, profileImage);
            return Ok(response);
        }
        [HttpPut("password")]
        public async Task<IActionResult> UpdatePassword([FromBody] UserPasswordDto dto)
        {
            var userId = GetUserIdFromToken();
            var response = await _userService.UpdatePasswordAsync(userId, dto);
            return Ok(response);
        }

        [HttpDelete("me")]
        public async Task<IActionResult> DeleteProfile()
        {
            var userId = GetUserIdFromToken();
            var response = await _userService.DeleteProfileAsync(userId);
            return Ok(response);
        }
        [HttpDelete]
        public async Task<IActionResult> DeleteProfileById(string userId)
        {
            var response = await _userService.DeleteProfileAsync(userId);
            return Ok(response);
        }
    }
}
