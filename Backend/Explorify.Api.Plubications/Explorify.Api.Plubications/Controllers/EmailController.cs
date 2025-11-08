using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;

namespace Explorify.Api.Publications.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class EmailController : ControllerBase
    {
        private readonly IEmailService _emailService;

        public EmailController(IEmailService emailService)
        {
            _emailService = emailService;
        }

        [HttpPost("send")]
        public async Task<IActionResult> SendEmail([FromBody] EmailDto email)
        {
            var result = await _emailService.SendEmailAsync(email);
            if (!result.IsSuccess)
                return BadRequest(result);

            return Ok(result);
        }
    }
}
