using Explorify.Api.Notifications.Application.Interfaces;
using Explorify.Api.Notifications.Application.Dtos;
using Microsoft.AspNetCore.Mvc;

namespace Explorify.Api.Notifications.Presentation.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class NotificationController : ControllerBase
    {
        private readonly INotificationService _service;

        public NotificationController(INotificationService service)
        {
            _service = service;
        }

        [HttpPost("send")]
        public async Task<IActionResult> Send(NotificationRequestDto dto)
        {
            return Ok(await _service.SendNotificationAsync(dto));
        }

        [HttpGet("user/{id}")]
        public async Task<IActionResult> Get(string id)
        {
            return Ok(await _service.GetNotificationsByUserAsync(id));
        }
    }
}
