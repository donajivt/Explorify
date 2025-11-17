using Logueo.Application.Dtos;
using Microsoft.AspNetCore.Http;

namespace Logueo.Application.Interfaces
{
    public interface IAuthService
    {
        Task<string> Register(RegistrationRequestDto registrationRequestDto, IFormFile? profileImage);
        Task<LoginResponseDto> Login(LoginRequestDto loginRequestDto);
        Task<bool> AssignRole(string email, string roleName);
        Task<IEnumerable<UserDto>> GetUsers();
        Task<UserDto?> UpdateUser(string userId, UpdateUserDto updateDto, IFormFile? profileImage);
        Task<UserDto?> GetUserById(string userId);
    }
}