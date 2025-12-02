using MongoDB.Bson.Serialization.Attributes;

namespace Logueo.Domain.Entities
{
    [BsonIgnoreExtraElements]
    public class User
    {
        public string Id { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string PasswordHash { get; set; } = string.Empty;
        public List<string> Roles { get; set; } = new();
        public string? ProfileImageUrl { get; set; }
        public string? DeviceToken { get; set; }
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }
}