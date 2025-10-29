using Explorify.Api.Users.Aplication.Dtos;
using Explorify.Api.Users.Aplication.Dtos.Explorify.Api.Users.Application.Dtos;
using Explorify.Api.Users.Aplication.Interfaces;
using Explorify.Api.Users.Application.Interfaces;
using Explorify.Api.Users.Domain.Entities;
using System;
using System.Threading.Tasks;

namespace Explorify.Api.Users.Application.Services
{
    public class UserService : IUserService
    {
        private readonly IUserRepository _userRepository;

        public UserService(IUserRepository userRepository)
        {
            _userRepository = userRepository;
        }

        public async Task<ResponseDto> GetProfileAsync(string userId)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
                return new ResponseDto { IsSuccess = false, Message = "Usuario no encontrado" };

            var dto = new UserDto
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                Role = user.Role
            };

            return new ResponseDto { Result = dto };
        }

        public async Task<ResponseDto> UpdateProfileAsync(string userId, UserUpdateDto dto)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
                return new ResponseDto { IsSuccess = false, Message = "Usuario no encontrado" };

            user.Username = dto.Username;
            user.Email = dto.Email;
            user.UpdatedAt = DateTime.UtcNow;

            await _userRepository.UpdateAsync(user);

            return new ResponseDto { Result = "Perfil actualizado correctamente" };
        }

        public async Task<ResponseDto> DeleteProfileAsync(string userId)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
                return new ResponseDto { IsSuccess = false, Message = "Usuario no encontrado" };

            await _userRepository.DeleteAsync(userId);
            return new ResponseDto { Result = "Cuenta eliminada correctamente" };
        }
    }
}
