using Explorify.Api.User.Application.Dtos;
using Microsoft.AspNetCore.Http;
using System.Threading.Tasks;

namespace Explorify.Api.User.Application.Interfaces
{
    public interface IUserService
    {
        Task<ResponseDto> GetProfileAsync(string userId);
        Task<ResponseDto> UpdateProfileAsync(string userId, UserUpdateDto dto, IFormFile? profileImage);
        Task<ResponseDto> DeleteProfileAsync(string userId);
        Task<ResponseDto> GetAllUsersAsync();
        Task<ResponseDto> GetUserByIdAsync(string id);
        Task<ResponseDto> UpdatePasswordAsync(string userId, UserPasswordDto dto);
        Task<ResponseDto> UpdateDeviceTokenAsync(string userId, UserDeviceTokenUpdate dto);
    }
}
