using Explorify.Api.Notifications.Application.Interfaces;
using Explorify.Api.Notifications.Application.Dtos;
using FirebaseAdmin;
using FirebaseAdmin.Messaging;
using Google.Apis.Auth.OAuth2;
using System;

namespace Explorify.Api.Notifications.Application.Services
{
    public class NotificationService : INotificationService
    {
        private readonly INotificationRepository _repository;

        public NotificationService(INotificationRepository repository)
        {
            _repository = repository;

            var jsonCredential = Environment.GetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS_JSON");

            if (string.IsNullOrEmpty(jsonCredential))
                throw new Exception("❌ La variable de entorno GOOGLE_APPLICATION_CREDENTIALS_JSON no está configurada.");

            if (FirebaseApp.DefaultInstance == null)
            {
                FirebaseApp.Create(new AppOptions()
                {
                    Credential = GoogleCredential.FromJson(jsonCredential)
                });
            }
        }

        public async Task<ResponseDto> SendNotificationAsync(NotificationRequestDto request)
        {
            try
            {
                var msg = new Message()
                {
                    Token = request.DeviceToken,
                    Notification = new FirebaseAdmin.Messaging.Notification
                    {
                        Title = request.Title,
                        Body = request.Message
                    }
                };

                string response = await FirebaseMessaging.DefaultInstance.SendAsync(msg);

                await _repository.SaveAsync(new Domain.Entities.Notification
                {
                    UserId = request.UserId,
                    Title = request.Title,
                    Message = request.Message,
                    DeviceToken = request.DeviceToken
                });

                return new ResponseDto { Result = response, Message = "📨 Notificación enviada correctamente" };
            }
            catch (Exception ex)
            {
                return new ResponseDto { IsSuccess = false, Message = $"⚠ Error enviando notificación: {ex.Message}" };
            }
        }

        public async Task<ResponseDto> GetNotificationsByUserAsync(string userId)
        {
            var data = await _repository.GetByUserAsync(userId);
            return new ResponseDto { Result = data };
        }
    }
}
