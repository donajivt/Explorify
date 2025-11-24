using Explorify.Api.Notifications.Application.Interfaces;
using Explorify.Api.Notifications.Application.Services;
using Explorify.Api.Notifications.Infrastructure.Notifications;
using Explorify.Api.Notifications.Infrastructure.Persistence;
using Explorify.Api.Notifications.Infrastructure.Repositories;
using Microsoft.AspNetCore.Cors.Infrastructure;

var builder = WebApplication.CreateBuilder(args);

// MongoDB Options
builder.Services.Configure<MongoOptions>(
    builder.Configuration.GetSection(MongoOptions.SectionName));

var mongoOptions = new MongoOptions();
builder.Configuration.GetSection(MongoOptions.SectionName).Bind(mongoOptions);

// Contexto Mongo
builder.Services.AddSingleton(new MongoContext(mongoOptions));

// Repositorio y servicios
builder.Services.AddScoped<INotificationRepository, NotificationRepository>();
builder.Services.AddScoped<INotificationService, NotificationService>();

// JWT y Swagger
builder.AddAppAuthentication();
builder.Services.AddSwaggerWhitJwtAuthentication();

builder.Services.AddControllers();

var app = builder.Build();
app.UseSwagger();
app.UseSwaggerUI();

app.UseHttpsRedirection();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.Run();