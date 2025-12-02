using Logueo.Application.Dtos;
using Microsoft.AspNetCore.Http;

namespace Logueo.Application.Interfaces
{
    public interface IAuthService
    {
        Task<string> Register(RegistrationRequestDto registrationRequestDto);
        Task<LoginResponseDto> Login(LoginRequestDto loginRequestDto);
        Task<bool> AssignRole(string email, string roleName);
        Task<IEnumerable<UserDto>> GetUsers();
        Task<UserDto?> GetUserById(string userId);
        Task<ResponseDto> UpdateDeviceTokenAsync(string userId);
    }
}