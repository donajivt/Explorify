using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Logueo.Application.Dtos
{
    public class RegisterRequestDto
    {
        public string Id {  get; set; }
        public string Correo { get; set; }
        public string Nombre { get; set; }
        public string Password { get; set; }
    }
}
