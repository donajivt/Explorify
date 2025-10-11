using Logueo.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Logueo.Application.Interfaces
{
    public interface IJwtGenerator
    {
        string GenerateToken(User user, IEnumerable<string> roles);
    }
}
