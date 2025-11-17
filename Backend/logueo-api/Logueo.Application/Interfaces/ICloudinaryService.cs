using Microsoft.AspNetCore.Http;

namespace Logueo.Application.Interfaces
{
    public interface ICloudinaryService
    {
        Task<(string imageUrl, string publicId)?> UploadImageAsync(IFormFile file);
        Task<bool> DeleteImageAsync(string publicId);
    }
}