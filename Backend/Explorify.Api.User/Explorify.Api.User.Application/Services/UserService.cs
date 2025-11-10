using Explorify.Api.User.Application.Dtos;
using Explorify.Api.User.Application.Interfaces;
using Explorify.Api.User.Domain.Entities;
using System;
using System.Threading.Tasks;

namespace Explorify.Api.User.Application.Services
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
                Username = user.Name,
                Email = user.Email,
                Role = string.Join(", ", user.Roles)
            };

            return new ResponseDto { Result = dto };
        }

        public async Task<ResponseDto> UpdateProfileAsync(string userId, UserUpdateDto dto)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
                return new ResponseDto { IsSuccess = false, Message = "Usuario no encontrado" };

            user.Name = dto.Username;
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
        public async Task<ResponseDto> GetAllUsersAsync()
        {
            var response = new ResponseDto();
            try
            {
                var users = await _userRepository.GetAllUsersAsync();
                response.Result = users;
            }
            catch (Exception ex)
            {
                response.IsSuccess = false;
                response.Message = ex.Message;
            }
            return response;
        }

        public async Task<ResponseDto> GetUserByIdAsync(string id)
        {
            var response = new ResponseDto();
            try
            {
                var user = await _userRepository.GetUserByIdAsync(id);
                if (user == null)
                {
                    response.IsSuccess = false;
                    response.Message = "Usuario no encontrado.";
                }
                else
                {
                    response.Result = user;
                }
            }
            catch (Exception ex)
            {
                response.IsSuccess = false;
                response.Message = ex.Message;
            }
            return response;
        }

    }
}
