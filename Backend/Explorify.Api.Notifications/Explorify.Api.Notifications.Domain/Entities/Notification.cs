using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Notifications.Domain.Entities
{
    public class Notification
    {
        public string Id { get; set; } = "";
        public string UserId { get; set; } = "";
        public string Title { get; set; } = "";
        public string Message { get; set; } = "";
        public DateTime Date { get; set; } = DateTime.UtcNow;
        public string DeviceToken { get; set; } = "";
    }
}
