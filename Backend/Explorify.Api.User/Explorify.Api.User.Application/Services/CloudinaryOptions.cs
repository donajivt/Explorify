namespace Explorify.Api.User.Application.Services
{
    public class CloudinaryOptions
    {
        public const string SectionName = "Cloudinary";
        public string CloudName { get; set; } = "";
        public string ApiKey { get; set; } = "";
        public string ApiSecret { get; set; } = "";
    }
}