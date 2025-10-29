using Microsoft.AspNetCore.Http;

namespace Explorify.Api.Publications.Application.Dtos
{
    public class MediaUploadDto
    {
        public IFormFile File { get; set; }
        public string? Folder { get; set; }
    }
}