using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Application.Dtos
{
    public class ReportPublicationRequestDto
    {
        public string PublicationId { get; set; } = string.Empty;
        public string ReportedByUserId { get; set; } = string.Empty;
        public string Reason { get; set; } = string.Empty;
        public string? Description { get; set; }
    }
}
