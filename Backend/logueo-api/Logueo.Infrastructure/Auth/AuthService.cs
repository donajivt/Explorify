using Logueo.Application.Dtos;
using Logueo.Application.Interfaces;
using Logueo.Domain.Entities;
using Logueo.Infrastructure.Repositories;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Logueo.Infrastructure.Auth
{
    public class AuthService:IAuthService
    {
        private readonly IUserRepository _users;
        private readonly IJwtGenerator _jwt;

        public AuthService(IUserRepository users, IJwtGenerator jwt)
        {
            _users = users;
            _jwt = jwt;
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
                User = new UserDto { Id = user.Id, Email = user.Email, Name = user.Name },
                Token = token
            };
        }

        public async Task<string> Register(RegistrationRequestDto registrationRequestDto)
        {
            var email = registrationRequestDto.Email.Trim().ToLowerInvariant();
            var exists = await _users.GetByEmailAsync(email);
            if (exists is not null) return "El correo ya está registrado.";

            var user = new User
            {
                Email = email,
                Name = registrationRequestDto.Name,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(registrationRequestDto.Password),
                Roles = new List<string> {
                string.IsNullOrWhiteSpace(registrationRequestDto.Role) ? "user" : registrationRequestDto.Role
            }
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
                Email = u.Email
            }).ToList();

            return userDtos;
        }
    }
}
