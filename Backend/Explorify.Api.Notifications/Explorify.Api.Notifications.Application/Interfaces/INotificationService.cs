using Explorify.Api.Notifications.Application.Dtos;

namespace Explorify.Api.Notifications.Application.Interfaces
{
    public interface INotificationService
    {
        Task<ResponseDto> SendNotificationAsync(NotificationRequestDto request);
        Task<ResponseDto> GetNotificationsByUserAsync(string userId);
    }
}
