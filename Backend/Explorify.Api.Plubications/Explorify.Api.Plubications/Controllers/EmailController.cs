using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Infraestructure.Services;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class EmailController : ControllerBase
    {
        private readonly IEmailService _emailService;
        private readonly IEmailVerificationService _emailVerificationService;

        public EmailController(IEmailService emailService, IEmailVerificationService emailVerificationService)
        {
            _emailService = emailService;
            _emailVerificationService = emailVerificationService;
        }

        [HttpPost("send")]
        public async Task<IActionResult> SendEmail([FromBody] EmailDto email)
        {
            var result = await _emailService.SendEmailAsync(email);
            if (!result.IsSuccess)
                return BadRequest(result);

            return Ok(result);
        }
        [HttpPost("verify")]
        public async Task<IActionResult> VerifyEmail([FromBody] EmailVerificationRequestDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Email))
                return BadRequest("Email invalid");

            var result = await _emailVerificationService.VerifyEmail(dto.Email);

            if (!result.Success)
                return BadRequest(result);

            return Ok(result);
        }
    }
}
