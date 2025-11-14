using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Application.Dtos
{
    public class EmailVerificationResponseDto
    {
        public bool Success { get; set; }
        public string Status { get; set; }
        public string SubStatus { get; set; }
        public string Account { get; set; }
        public string Domain { get; set; }
        public string ErrorMessage { get; set; }
    }
}
