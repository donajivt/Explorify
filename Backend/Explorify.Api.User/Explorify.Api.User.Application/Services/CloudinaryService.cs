using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Explorify.Api.User.Application.Interfaces;
using Microsoft.AspNetCore.Http;

namespace Explorify.Api.User.Application.Services
{
    public class CloudinaryService : ICloudinaryService
    {
        private readonly Cloudinary _cloudinary;

        public CloudinaryService(CloudinaryOptions options)
        {
            var account = new Account(
                options.CloudName,
                options.ApiKey,
                options.ApiSecret
            );
            _cloudinary = new Cloudinary(account);
        }

        public async Task<(string imageUrl, string publicId)?> UploadImageAsync(IFormFile file)
        {
            if (file == null || file.Length == 0)
                return null;

            // Validar tipo de archivo
            var allowedTypes = new[] { "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp" };
            if (!allowedTypes.Contains(file.ContentType.ToLower()))
                return null;

            // Validar tamaño (máximo 5MB)
            if (file.Length > 5 * 1024 * 1024)
                return null;

            using var stream = file.OpenReadStream();
            var uploadParams = new ImageUploadParams
            {
                File = new FileDescription(file.FileName, stream),
                Transformation = new Transformation()
                    .Width(500)
                    .Height(500)
                    .Crop("fill")
                    .Gravity("face"),
                Folder = "explorify/profiles"
            };

            var uploadResult = await _cloudinary.UploadAsync(uploadParams);

            if (uploadResult.StatusCode == System.Net.HttpStatusCode.OK)
            {
                return (uploadResult.SecureUrl.ToString(), uploadResult.PublicId);
            }

            return null;
        }

        public async Task<bool> DeleteImageAsync(string publicId)
        {
            if (string.IsNullOrEmpty(publicId))
                return false;

            var deleteParams = new DeletionParams(publicId);
            var result = await _cloudinary.DestroyAsync(deleteParams);

            return result.Result == "ok";
        }
    }
}