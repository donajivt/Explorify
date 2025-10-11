using Logueo.Application.Interfaces;
using Logueo.Infrastructure.Auth;
using Logueo.Infrastructure.Persistence;
using Logueo.Infrastructure.Repositories;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace Logueo.Infrastructure
{
    public static class DepencyInjection
    {
        public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration cfg)
        {
            // Mongo
            var mongo = new MongoOptions();
            cfg.GetSection(MongoOptions.SectionName).Bind(mongo);
            services.AddSingleton(mongo);
            services.AddSingleton<MongoContext>();

            // JWT
            var jwt = new JwtOptions();
            cfg.GetSection("ApiSettings:JwtOptions").Bind(jwt);
            services.AddSingleton(jwt);

            // Repos + servicios
            services.AddScoped<IUserRepository, UserRepository>();
            services.AddScoped<IJwtGenerator, JwtGenerator>();
            services.AddScoped<IAuthService, AuthService>();

            return services;
        }
    }
}
