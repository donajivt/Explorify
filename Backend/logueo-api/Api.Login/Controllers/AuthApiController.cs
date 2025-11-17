using Api.Login.Contract;
using Logueo.Application.Dtos;
using Logueo.Application.Interfaces;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace Api.Login.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthApiController : ControllerBase
    {
        private readonly IAuthService _authService;
        private readonly ApiResponse _response = new();

        public AuthApiController(IAuthService authService)
        {
            _authService = authService;
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegistrationRequestDto model)
        {
            var errorMessage = await _authService.Register(model);
            if (!string.IsNullOrEmpty(errorMessage))
            {
                _response.IsSuccess = false;
                _response.Message = errorMessage;
                return BadRequest(_response);
            }
            return Ok(_response);
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequestDto model)
        {
            var loginResponse = await _authService.Login(model);
            if (loginResponse.User == null)
            {
                _response.IsSuccess = false;
                _response.Message = "El nombre de usuario o la contraseña es incorrecto";
                return BadRequest(_response);
            }
            _response.Result = loginResponse;
            return Ok(_response);
        }

        [HttpPost("assign-role")]
        public async Task<IActionResult> AssignRole([FromBody] RegistrationRequestDto model)
        {
            var ok = await _authService.AssignRole(model.Email, model.Role.ToUpperInvariant());
            if (!ok)
            {
                _response.IsSuccess = false;
                _response.Message = "Error de asignación de rol";
                return BadRequest(_response);
            }
            return Ok(_response);
        }

        [HttpPost("logout")]
        public IActionResult Logout()
        {
            _response.IsSuccess = true;
            _response.Message = "Sesión cerrada correctamente";
            return Ok(_response);
        }

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var users = await _authService.GetUsers();

            if (users == null || !users.Any())
            {
                _response.IsSuccess = false;
                _response.Message = "No se encontraron usuarios";
                return NotFound(_response);
            }

            _response.IsSuccess = true;
            _response.Message = "Usuarios obtenidos correctamente";
            _response.Result = users;
            return Ok(_response);
        }

        [HttpGet("{userId}")]
        public async Task<IActionResult> GetUserById(string userId)
        {
            var user = await _authService.GetUserById(userId);

            if (user == null)
            {
                _response.IsSuccess = false;
                _response.Message = "Usuario no encontrado";
                return NotFound(_response);
            }

            _response.IsSuccess = true;
            _response.Message = "Usuario obtenido correctamente";
            _response.Result = user;
            return Ok(_response);
        }
    }
}