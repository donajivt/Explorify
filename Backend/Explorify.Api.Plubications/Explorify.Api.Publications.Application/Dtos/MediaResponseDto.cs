namespace Explorify.Api.Publications.Application.Dtos
{
    public class MediaResponseDto
    {
        public string PublicId { get; set; } = string.Empty;
        public string Url { get; set; } = string.Empty;
        public string SecureUrl { get; set; } = string.Empty;
        public string Format { get; set; } = string.Empty;
        public string ResourceType { get; set; } = string.Empty;
    }
}