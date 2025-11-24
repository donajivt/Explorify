using Explorify.Api.Likes.Application.Interfaces;
using Explorify.Api.Likes.Application.Services;
using Explorify.Api.Likes.Infrastructure.Extensions;
using Explorify.Api.Likes.Infrastructure.Persistence;
using Explorify.Api.Likes.Infrastructure.Repositories;

var builder = WebApplication.CreateBuilder(args);

// MongoDB Options
builder.Services.Configure<MongoOptions>(
    builder.Configuration.GetSection(MongoOptions.SectionName));

var mongoOptions = new MongoOptions();
builder.Configuration.GetSection(MongoOptions.SectionName).Bind(mongoOptions);

// Contexto Mongo
builder.Services.AddSingleton(new MongoContext(mongoOptions));

// Repositorio y servicios
builder.Services.AddScoped<ILikeRepository, LikeRepository>();
builder.Services.AddScoped<ILikeService, LikeService>();

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

public partial class Program { }
