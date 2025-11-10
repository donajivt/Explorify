using Explorify.Api.User.Application.Dtos;
using System.Threading.Tasks;

namespace Explorify.Api.User.Application.Interfaces
{
    public interface IUserService
    {
        Task<ResponseDto> GetProfileAsync(string userId);
        Task<ResponseDto> UpdateProfileAsync(string userId, UserUpdateDto dto);
        Task<ResponseDto> DeleteProfileAsync(string userId);
        Task<ResponseDto> GetAllUsersAsync();
        Task<ResponseDto> GetUserByIdAsync(string id);

    }
}
