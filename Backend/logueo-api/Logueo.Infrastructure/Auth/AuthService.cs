using Logueo.Application.Dtos;
using Logueo.Application.Interfaces;
using Logueo.Domain.Entities;
using Microsoft.AspNetCore.Http;

namespace Logueo.Infrastructure.Auth
{
    public class AuthService : IAuthService
    {
        private readonly IUserRepository _users;
        private readonly IJwtGenerator _jwt;
        private readonly ICloudinaryService _cloudinary;

        public AuthService(IUserRepository users, IJwtGenerator jwt, ICloudinaryService cloudinary)
        {
            _users = users;
            _jwt = jwt;
            _cloudinary = cloudinary;
        }

        public async Task<LoginResponseDto> Login(LoginRequestDto loginRequestDto)
        {
            var email = (loginRequestDto.UserName ?? "").Trim().ToLowerInvariant();
            var user = await _users.GetByEmailAsync(email);
            if (user is null) return new LoginResponseDto { User = null, Token = "" };

            var ok = BCrypt.Net.BCrypt.Verify(loginRequestDto.Password, user.PasswordHash);
            if (!ok) return new LoginResponseDto { User = null, Token = "" };

            var roles = await _users.GetRolesAsync(user.Email);
            var token = _jwt.GenerateToken(user, roles);

            return new LoginResponseDto
            {
                User = new UserDto
                {
                    Id = user.Id,
                    Email = user.Email,
                    Name = user.Name,
                    ProfileImageUrl = user.ProfileImageUrl
                },
                Token = token
            };
        }

        public async Task<string> Register(RegistrationRequestDto registrationRequestDto, IFormFile? profileImage)
        {
            var email = registrationRequestDto.Email.Trim().ToLowerInvariant();
            var exists = await _users.GetByEmailAsync(email);
            if (exists is not null) return "El correo ya está registrado.";

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

            var user = new User
            {
                Email = email,
                Name = registrationRequestDto.Name,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(registrationRequestDto.Password),
                Roles = new List<string>
                {
                    string.IsNullOrWhiteSpace(registrationRequestDto.Role) ? "user" : registrationRequestDto.Role
                },
                ProfileImageUrl = profileImageUrl,
                CloudinaryPublicId = cloudinaryPublicId
            };

            await _users.AddAsync(user);
            return "";
        }

        public Task<bool> AssignRole(string email, string roleName)
            => _users.AddRoleAsync(email.Trim().ToLowerInvariant(), roleName.Trim());

        public async Task<IEnumerable<UserDto>> GetUsers()
        {
            var users = await _users.GetAllUsers();

            var userDtos = users.Select(u => new UserDto
            {
                Id = u.Id,
                Name = u.Name,
                Email = u.Email,
                ProfileImageUrl = u.ProfileImageUrl
            }).ToList();

            return userDtos;
        }

        public async Task<UserDto?> GetUserById(string userId)
        {
            var user = await _users.GetByIdAsync(userId);
            if (user == null) return null;

            return new UserDto
            {
                Id = user.Id,
                Email = user.Email,
                Name = user.Name,
                ProfileImageUrl = user.ProfileImageUrl
            };
        }

        public async Task<UserDto?> UpdateUser(string userId, UpdateUserDto updateDto, IFormFile? profileImage)
        {
            var user = await _users.GetByIdAsync(userId);
            if (user == null) return null;

            // Actualizar nombre si se proporciona
            if (!string.IsNullOrWhiteSpace(updateDto.Name))
            {
                user.Name = updateDto.Name.Trim();
            }

            // Actualizar email si se proporciona
            if (!string.IsNullOrWhiteSpace(updateDto.Email))
            {
                var newEmail = updateDto.Email.Trim().ToLowerInvariant();
                if (newEmail != user.Email)
                {
                    // Verificar que el nuevo email no exista
                    var existingUser = await _users.GetByEmailAsync(newEmail);
                    if (existingUser != null)
                        return null; // Email ya existe

                    user.Email = newEmail;
                }
            }

            // Actualizar imagen si se proporciona
            if (profileImage != null)
            {
                // Eliminar imagen anterior si existe
                if (!string.IsNullOrEmpty(user.CloudinaryPublicId))
                {
                    await _cloudinary.DeleteImageAsync(user.CloudinaryPublicId);
                }

                // Subir nueva imagen
                var uploadResult = await _cloudinary.UploadImageAsync(profileImage);
                if (uploadResult.HasValue)
                {
                    user.ProfileImageUrl = uploadResult.Value.imageUrl;
                    user.CloudinaryPublicId = uploadResult.Value.publicId;
                }
            }

            await _users.UpdateAsync(user);

            return new UserDto
            {
                Id = user.Id,
                Email = user.Email,
                Name = user.Name,
                ProfileImageUrl = user.ProfileImageUrl
            };
        }
    }
}