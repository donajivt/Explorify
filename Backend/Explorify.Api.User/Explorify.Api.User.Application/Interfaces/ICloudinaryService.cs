using Microsoft.AspNetCore.Http;

namespace Explorify.Api.User.Application.Interfaces
{
    public interface ICloudinaryService
    {
        Task<(string imageUrl, string publicId)?> UploadImageAsync(IFormFile file);
        Task<bool> DeleteImageAsync(string publicId);
    }
}