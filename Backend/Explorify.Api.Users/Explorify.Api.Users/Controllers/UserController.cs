using Explorify.Api.Users.Aplication.Dtos;
using Explorify.Api.Users.Aplication.Interfaces;
using Explorify.Api.Users.Application.Interfaces;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;

namespace Explorify.Api.Users.Api.Controllers
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
            return User.FindFirstValue(ClaimTypes.NameIdentifier)
                ?? User.FindFirstValue(JwtRegisteredClaimNames.Sub)
                ?? string.Empty;
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
