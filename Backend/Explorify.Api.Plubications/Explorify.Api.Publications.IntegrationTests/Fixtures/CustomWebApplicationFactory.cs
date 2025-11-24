using DotNet.Testcontainers.Builders;
using Explorify.Api.Publications.Infraestructure.Persistence;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.VisualStudio.TestPlatform.TestHost;
using Testcontainers.MongoDb;
using Xunit;

namespace Explorify.Api.Publications.IntegrationTests.Fixtures
{
    public class CustomWebApplicationFactory : WebApplicationFactory<Program>, IAsyncLifetime
    {
        private readonly MongoDbContainer _mongoContainer;

        public CustomWebApplicationFactory()
        {
            _mongoContainer = new MongoDbBuilder()
                .WithImage("mongo:7.0")
                .WithPortBinding(27017, true)
                .WithWaitStrategy(Wait.ForUnixContainer().UntilPortIsAvailable(27017))
                .Build();
        }

        protected override void ConfigureWebHost(IWebHostBuilder builder)
        {

            builder.ConfigureAppConfiguration((context, config) =>
            {
                config.AddInMemoryCollection(new Dictionary<string, string>
                {
                    ["Mongo:ConnectionString"] = _mongoContainer.GetConnectionString(),
                    ["Mongo:Database"] = "TestDatabase",
                    ["Mongo:PublicationsCollection"] = "publications",
                    ["Mongo:ReportsCollection"] = "publicationReports",
                    ["ApiSettings:JwtOptions:Secret"] = "JKDJKADHKDKJDODEWEJUWDI=SAM?NSHSAJKSAJKSAHKSAKJSA",
                    ["ApiSettings:JwtOptions:Issuer"] = "test-issuer",
                    ["ApiSettings:JwtOptions:Audience"] = "test-audience",
                    ["Cloudinary:CloudName"] = "test-cloud",
                    ["Cloudinary:ApiKey"] = "test-key",
                    ["Cloudinary:ApiSecret"] = "test-secret",
                    ["Cloudinary:DefaultFolder"] = "test-folder",
                    ["EmailSettings:FromEmail"] = "test@test.com",
                    ["EmailSettings:FromName"] = "Test",
                    ["EmailSettings:SmtpHost"] = "smtp.test.com",
                    ["EmailSettings:SmtpPort"] = "587",
                    ["EmailSettings:SmtpUser"] = "test@test.com",
                    ["EmailSettings:SmtpPass"] = "testpass",
                    ["ZeroBounce:PrimaryKey"] = "test-key",
                    ["ZeroBounce:BackupKey"] = "backup-key"
                });
            });

            builder.UseEnvironment("Testing");
        }

        public async Task InitializeAsync()
        {
            await _mongoContainer.StartAsync();
        }

        public new async Task DisposeAsync()
        {
            await _mongoContainer.DisposeAsync();
        }

        public MongoContext GetMongoContext()
        {
            var options = new MongoOptions
            {
                ConnectionString = _mongoContainer.GetConnectionString(),
                Database = "TestDatabase",
                PublicationsCollection = "publications",
                ReportsCollection = "publicationReports"
            };

            return new MongoContext(options);
        }
    }
}