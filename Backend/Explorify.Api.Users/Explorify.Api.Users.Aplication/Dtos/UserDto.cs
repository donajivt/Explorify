using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Users.Aplication.Dtos
{
    namespace Explorify.Api.Users.Application.Dtos
    {
        public class UserDto
        {
            public string Id { get; set; } = string.Empty;
            public string Username { get; set; } = string.Empty;
            public string Email { get; set; } = string.Empty;
            public string Role { get; set; } = string.Empty;
        }
    }

}
