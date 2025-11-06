namespace Explorify.Api.Likes.Application.Dtos
{
    public class LikeDto
    {
        public string Id { get; set; } = string.Empty;
        public string PublicationId { get; set; } = string.Empty;
        public string UserId { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
    }

    public class LikeRequestDto
    {
        public string PublicationId { get; set; } = string.Empty;
    }

    public class LikeStatsDto
    {
        public string PublicationId { get; set; } = string.Empty;
        public int TotalLikes { get; set; }
        public bool UserHasLiked { get; set; }
    }

    public class ResponseDto
    {
        public object Result { get; set; }
        public bool IsSuccess { get; set; } = true;
        public string Message { get; set; } = "";
    }
}