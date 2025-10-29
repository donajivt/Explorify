using Explorify.Api.Publications.Application.Dtos;
using Microsoft.AspNetCore.Http;

namespace Explorify.Api.Publications.Application.Interfaces
{
    public interface ICloudinaryService
    {
        Task<MediaResponseDto> UploadImageAsync(IFormFile file, string? folder = null);
        Task<MediaResponseDto> UploadVideoAsync(IFormFile file, string? folder = null);
        Task<bool> DeleteMediaAsync(string publicId);
        Task<MediaResponseDto> UpdateImageAsync(string publicId, IFormFile newFile);
        string GetOptimizedUrl(string publicId, int width = 800, int height = 600);
    }
}