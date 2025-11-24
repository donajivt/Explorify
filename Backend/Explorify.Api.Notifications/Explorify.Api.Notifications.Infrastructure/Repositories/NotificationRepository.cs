using Explorify.Api.Notifications.Application.Interfaces;
using Explorify.Api.Notifications.Domain.Entities;
using Explorify.Api.Notifications.Infrastructure.Persistence;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Notifications.Infrastructure.Repositories
{
    public class NotificationRepository : INotificationRepository
    {
        private readonly MongoContext _context;

        public NotificationRepository(MongoContext context)
        {
            _context = context;
        }

        public async Task SaveAsync(Notification notification)
        {
            await _context.Notification.InsertOneAsync(notification);
        }

        public async Task<List<Notification>> GetByUserAsync(string userId)
        {
            return await _context.Notification
                .Find(n => n.UserId == userId)
                .ToListAsync();
        }
    }
}