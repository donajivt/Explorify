using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Infraestructure.Options;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Options;
using System;
using System.Linq;

namespace Explorify.Api.Publications.Infraestructure.Services
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
                Overwrite = false,
                Moderation = "aws_rek"
            };

            var result = await _cloudinary.UploadAsync(uploadParams);

            if (result.Error != null)
                throw new Exception($"Error al subir imagen: {result.Error.Message}");

            if (result.Moderation != null && result.Moderation.Count > 0)
            {
                var moderationResult = result.Moderation.FirstOrDefault(m => m.Kind == "aws_rek");

                if (moderationResult != null && moderationResult.Status == ModerationStatus.Rejected)
                {
                    await _cloudinary.DeleteResourcesAsync(ResourceType.Image, result.PublicId);

                    throw new Exception("La imagen fue rechazada por contener contenido inapropiado.");
                }
            }

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
                Overwrite = false,
                Moderation = "aws_rek"
            };

            var result = await _cloudinary.UploadAsync(uploadParams);

            if (result.Error != null)
                throw new Exception($"Error al subir video: {result.Error.Message}");

            if (result.Moderation != null && result.Moderation.Count > 0)
            {
                var moderationResult = result.Moderation.FirstOrDefault(m => m.Kind == "aws_rek");

                if (moderationResult != null && moderationResult.Status == ModerationStatus.Rejected)
                {
                    await _cloudinary.DeleteResourcesAsync(ResourceType.Video, result.PublicId);

                    throw new Exception("El video fue rechazado por contener contenido inapropiado.");
                }
            }

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
                ResourceType = ResourceType.Auto
            };

            var result = await _cloudinary.DestroyAsync(deleteParams);

            return result.Result.ToLower() == "ok" || result.Result.ToLower() == "not found";
        }

        public async Task<MediaResponseDto> UpdateImageAsync(string publicId, IFormFile newFile)
        {
            if (newFile == null || newFile.Length == 0)
                throw new ArgumentException("El archivo es inválido");
            if (string.IsNullOrWhiteSpace(publicId))
                throw new ArgumentException("El PublicId es inválido para actualizar");

            var uploadParams = new ImageUploadParams
            {
                File = new FileDescription(newFile.FileName, newFile.OpenReadStream()),
                PublicId = publicId,
                Overwrite = true,
                Invalidate = true,
                Moderation = "aws_rek"
            };

            var result = await _cloudinary.UploadAsync(uploadParams);

            if (result.Error != null)
                throw new Exception($"Error al actualizar imagen: {result.Error.Message}");

            if (result.Moderation != null && result.Moderation.Count > 0)
            {
                var moderationResult = result.Moderation.FirstOrDefault(m => m.Kind == "aws_rek");

                if (moderationResult != null && moderationResult.Status == ModerationStatus.Rejected)
                {
                    await _cloudinary.DeleteResourcesAsync(ResourceType.Image, result.PublicId);
                    throw new Exception("La nueva imagen fue rechazada por contener contenido inapropiado.");
                }
            }

            return new MediaResponseDto
            {
                PublicId = result.PublicId,
                Url = result.Url.ToString(),
                SecureUrl = result.SecureUrl.ToString(),
                Format = result.Format,
                ResourceType = result.ResourceType
            };
        }

        public string GetOptimizedUrl(string publicId, int width = 800, int height = 600)
        {
            var transformation = new Transformation()
                .Width(width)
                .Height(height)
                .Crop("fill")
                .Gravity("auto")
                .Quality("auto")
                .FetchFormat("auto");

            return _cloudinary.Api.UrlImgUp
                .Transform(transformation)
                .BuildUrl(publicId);
        }
    }
}