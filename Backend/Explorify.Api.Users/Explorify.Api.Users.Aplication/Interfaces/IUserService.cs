using Explorify.Api.Users.Aplication.Dtos;
using System.Threading.Tasks;

namespace Explorify.Api.Users.Application.Interfaces
{
    public interface IUserService
    {
        Task<ResponseDto> GetProfileAsync(string userId);
        Task<ResponseDto> UpdateProfileAsync(string userId, UserUpdateDto dto);
        Task<ResponseDto> DeleteProfileAsync(string userId);
    }
}
