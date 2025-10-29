using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Infrastructure.Options;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Options;


namespace Explorify.Api.Publications.Infrastructure.Services
{
    public class CloudinaryService : ICloudinaryService
    {
        private readonly Cloudinary _cloudinary;
        private readonly CloudinaryOptions _options;

        public CloudinaryService(IOptions<CloudinaryOptions> options)
        {
            _options = options.Value;

            var account = new Account(
                _options.CloudName,
                _options.ApiKey,
                _options.ApiSecret
            );

            _cloudinary = new Cloudinary(account);
            _cloudinary.Api.Secure = true;
        }

        public async Task<MediaResponseDto> UploadImageAsync(IFormFile file, string? folder = null)
        {
            if (file == null || file.Length == 0)
                throw new ArgumentException("El archivo es inválido");

            var uploadParams = new ImageUploadParams
            {
                File = new FileDescription(file.FileName, file.OpenReadStream()),
                Folder = folder ?? _options.DefaultFolder,
                Transformation = new Transformation()
                    .Quality("auto")
                    .FetchFormat("auto"),
                UniqueFilename = true,
                Overwrite = false
            };

            var result = await _cloudinary.UploadAsync(uploadParams);

            if (result.Error != null)
                throw new Exception($"Error al subir imagen: {result.Error.Message}");

            return new MediaResponseDto
            {
                PublicId = result.PublicId,
                Url = result.Url.ToString(),
                SecureUrl = result.SecureUrl.ToString(),
                Format = result.Format,
                ResourceType = result.ResourceType
            };
        }

        public async Task<MediaResponseDto> UploadVideoAsync(IFormFile file, string? folder = null)
        {
            if (file == null || file.Length == 0)
                throw new ArgumentException("El archivo es inválido");

            var uploadParams = new VideoUploadParams
            {
                File = new FileDescription(file.FileName, file.OpenReadStream()),
                Folder = folder ?? _options.DefaultFolder,
                Transformation = new Transformation()
                    .Quality("auto"),
                UniqueFilename = true,
                Overwrite = false
            };

            var result = await _cloudinary.UploadAsync(uploadParams);

            if (result.Error != null)
                throw new Exception($"Error al subir video: {result.Error.Message}");

            return new MediaResponseDto
            {
                PublicId = result.PublicId,
                Url = result.Url.ToString(),
                SecureUrl = result.SecureUrl.ToString(),
                Format = result.Format,
                ResourceType = result.ResourceType
            };
        }

        public async Task<bool> DeleteMediaAsync(string publicId)
        {
            if (string.IsNullOrWhiteSpace(publicId))
                return false;

            var deleteParams = new DeletionParams(publicId)
            {
                ResourceType = ResourceType.Image
            };

            var result = await _cloudinary.DestroyAsync(deleteParams);
            return result.Result == "ok";
        }

        public async Task<MediaResponseDto> UpdateImageAsync(string publicId, IFormFile newFile)
        {
            // Eliminar imagen anterior
            await DeleteMediaAsync(publicId);

            // Subir nueva imagen manteniendo la misma carpeta
            var folder = publicId.Contains("/")
                ? publicId.Substring(0, publicId.LastIndexOf("/"))
                : _options.DefaultFolder;

            return await UploadImageAsync(newFile, folder);
        }

        public string GetOptimizedUrl(string publicId, int width = 800, int height = 600)
        {
            var transformation = new Transformation()
                .Width(width)
                .Height(height)
                .Crop("fill")
                .Quality("auto")
                .FetchFormat("auto");

            return _cloudinary.Api.UrlImgUp
                .Transform(transformation)
                .BuildUrl(publicId);
        }
    }
}