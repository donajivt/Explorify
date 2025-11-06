namespace Explorify.Api.Likes.Infrastructure.Persistence
{
    public class MongoOptions
    {
        public const string SectionName = "Mongo";
        public string ConnectionString { get; set; } = "";
        public string Database { get; set; } = "";
        public string LikesCollection { get; set; } = "likes";
    }
}