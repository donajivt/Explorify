using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Notifications.Application.Dtos
{
    public class NotificationRequestDto
    {
        public string UserId { get; set; } = "";
        public string Title { get; set; } = "";
        public string Message { get; set; } = "";
        public string DeviceToken { get; set; } = "";
    }
}
