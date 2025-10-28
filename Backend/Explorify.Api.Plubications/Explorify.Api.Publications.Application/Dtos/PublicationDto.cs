namespace Explorify.Api.Publications.Application.Dtos
{
    public class PublicationDto
    {
        public string? Id { get; set; }
        public string ImageUrl { get; set; } = string.Empty;
        public string Title { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public string Location { get; set; } = string.Empty;
        public string? UserId { get; set; }
        public DateTime? CreatedAt { get; set; }
    }
}
