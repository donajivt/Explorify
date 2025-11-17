using CloudinaryDotNet;
using Explorify.Api.User.Application.Dtos;
using Explorify.Api.User.Application.Interfaces;
using Explorify.Api.User.Domain.Entities;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using System;
using BCrypt.Net;
using System.Threading.Tasks;

namespace Explorify.Api.User.Application.Services
{
    public class UserService : IUserService
    {
        private readonly IUserRepository _userRepository;
        private readonly ICloudinaryService _cloudinary;
        private readonly PasswordHasher<Domain.Entities.User> _passwordHasher;

        public UserService(IUserRepository userRepository, ICloudinaryService cloudinary)
        {
            _userRepository = userRepository;
            _cloudinary = cloudinary;
            _passwordHasher = new PasswordHasher<Domain.Entities.User>();
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
                Role = string.Join(", ", user.Roles),
                ProfileImageUrl = user.ProfileImageUrl,
                CloudinaryPublicId = user.CloudinaryPublicId,
            };

            return new ResponseDto { Result = dto };
        }

        public async Task<ResponseDto> UpdateProfileAsync(string userId, UserUpdateDto dto, IFormFile? profileImage)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
                return new ResponseDto { IsSuccess = false, Message = "Usuario no encontrado" };

            string? profileImageUrl = null;
            string? cloudinaryPublicId = null;
            // Si hay imagen, subirla a Cloudinary
            if (profileImage != null)
            {
                var uploadResult = await _cloudinary.UploadImageAsync(profileImage);
                if (uploadResult.HasValue)
                {
                    profileImageUrl = uploadResult.Value.imageUrl;
                    cloudinaryPublicId = uploadResult.Value.publicId;
                }
            }
            user.Name = dto.Username;
            user.Email = dto.Email;
            user.UpdatedAt = DateTime.UtcNow;
            user.ProfileImageUrl = profileImageUrl;
            user.CloudinaryPublicId = cloudinaryPublicId;

            await _userRepository.UpdateAsync(user);

            return new ResponseDto { Result = "Perfil actualizado correctamente" };
        }
        public async Task<ResponseDto> UpdatePasswordAsync(string userId, UserPasswordDto dto)
        {
            var user = await _userRepository.GetByIdAsync(userId);
            if (user == null)
                return new ResponseDto { IsSuccess = false, Message = "Usuario no encontrado" };

            // 1. Validar contraseña anterior usando BCrypt
            bool isPasswordCorrect = BCrypt.Net.BCrypt.Verify(dto.OldPassword, user.PasswordHash);

            if (!isPasswordCorrect)
            {
                return new ResponseDto
                {
                    IsSuccess = false,
                    Message = "La contraseña anterior no es correcta."
                };
            }

            // 2. Hashear la nueva contraseña
            string newHashedPassword = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);

            // 3. Guardar el nuevo hash
            user.PasswordHash = newHashedPassword;
            user.UpdatedAt = DateTime.UtcNow;

            await _userRepository.UpdateAsync(user);

            return new ResponseDto { Result = "Contraseña actualizada correctamente" };
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
