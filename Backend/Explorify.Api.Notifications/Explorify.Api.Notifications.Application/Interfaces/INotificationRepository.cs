using Explorify.Api.Notifications.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Notifications.Application.Interfaces
{
    public interface INotificationRepository
    {
        Task SaveAsync(Notification notification);
        Task<List<Notification>> GetByUserAsync(string userId);
    }
}
