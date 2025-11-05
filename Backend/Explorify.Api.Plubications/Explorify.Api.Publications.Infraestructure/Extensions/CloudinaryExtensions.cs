using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Infraestructure.Options;
using Explorify.Api.Publications.Infraestructure.Services;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace Explorify.Api.Publications.Infraestructure.Extensions
{
    public static class CloudinaryExtensions
    {
        public static IServiceCollection AddCloudinaryService(
            this IServiceCollection services,
            IConfiguration configuration)
        {
            // Configurar opciones de Cloudinary
            services.Configure<CloudinaryOptions>(
                configuration.GetSection(CloudinaryOptions.SectionName));

            // Registrar el servicio
            services.AddScoped<ICloudinaryService, CloudinaryService>();

            return services;
        }
    }
}