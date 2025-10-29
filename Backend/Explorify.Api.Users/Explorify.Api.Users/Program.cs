using Explorify.Api.Publications.Infraestructure.Publication;
using Explorify.Api.Users.Aplication.Interfaces;
using Explorify.Api.Users.Application.Interfaces;
using Explorify.Api.Users.Application.Services;
using Explorify.Api.Users.Infrastructure.Persistence;
using Explorify.Api.Users.Infrastructure.Repositories;

var builder = WebApplication.CreateBuilder(args);

// MongoDB Options
builder.Services.Configure<MongoOptions>(
    builder.Configuration.GetSection(MongoOptions.SectionName));

var mongoOptions = new MongoOptions();
builder.Configuration.GetSection(MongoOptions.SectionName).Bind(mongoOptions);

// Contexto Mongo
builder.Services.AddSingleton(new MongoContext(mongoOptions));

// Repositorio y servicios
builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<IUserService, UserService>();

// JWT y Swagger
builder.AddAppAuthentication();
builder.Services.AddSwaggerWhitJwtAuthentication();

// Controllers
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();

app.UseHttpsRedirection();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();