using Explorify.Api.Likes.Infrastructure.Persistence;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;
using Microsoft.VisualStudio.TestPlatform.TestHost;
using Testcontainers.MongoDb;
using Xunit;

namespace Explorify.Api.Likes.Tests.Integration
{
    public class CustomWebApplicationFactory : WebApplicationFactory<Program>, IAsyncLifetime
    {
        private MongoDbContainer _mongoContainer = null!;

        protected override void ConfigureWebHost(IWebHostBuilder builder)
        {
            builder.ConfigureServices(services =>
            {
                // Remover el MongoContext original
                services.RemoveAll<MongoContext>();

                // Agregar MongoContext de prueba con Testcontainers
                services.AddSingleton(sp =>
                {
                    var options = new MongoOptions
                    {
                        ConnectionString = _mongoContainer.GetConnectionString(),
                        Database = "LikesTestDb",
                        LikesCollection = "likes"
                    };
                    return new MongoContext(options);
                });

                // Aquí puedes agregar más configuraciones específicas para pruebas
                // Por ejemplo, deshabilitar autenticación o usar mocks
            });
        }

        public async Task InitializeAsync()
        {
            _mongoContainer = new MongoDbBuilder()
                .WithImage("mongo:7.0")
                .Build();

            await _mongoContainer.StartAsync();
        }

        async Task IAsyncLifetime.DisposeAsync()
        {
            await _mongoContainer.DisposeAsync();
        }
    }

    // Clase base para pruebas de integración que usan la factory personalizada
    public class IntegrationTestBase : IClassFixture<CustomWebApplicationFactory>
    {
        protected readonly CustomWebApplicationFactory Factory;
        protected readonly HttpClient Client;

        public IntegrationTestBase(CustomWebApplicationFactory factory)
        {
            Factory = factory;
            Client = factory.CreateClient();
        }
    }
}