﻿namespace Explorify.Api.Publications.Infrastructure.Options
{
    public class CloudinaryOptions
    {
        public const string SectionName = "Cloudinary";
        public string CloudName { get; set; } = string.Empty;
        public string ApiKey { get; set; } = string.Empty;
        public string ApiSecret { get; set; } = string.Empty;
        public string DefaultFolder { get; set; } = "explorify";
    }
}