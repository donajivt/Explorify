using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Application.Services;
using Explorify.Api.Publications.Domain.Interfaces;
using Explorify.Api.Publications.Infraestructure.Extensions;
using Explorify.Api.Publications.Infraestructure.Persistence;
using Explorify.Api.Publications.Infraestructure.Publication;
using Explorify.Api.Publications.Infraestructure.Repositories;
using Explorify.Api.Publications.Infraestructure.Services;

var builder = WebApplication.CreateBuilder(args);
var corsPolicy = "AllowAll";

builder.Services.AddCors(options =>
{
    options.AddPolicy(name: corsPolicy, policy =>
    {
        policy
            .AllowAnyOrigin()   
            .AllowAnyHeader()   
            .AllowAnyMethod();  
    });
});

// MongoDB Options
builder.Services.Configure<MongoOptions>(
    builder.Configuration.GetSection(MongoOptions.SectionName));

var mongoOptions = new MongoOptions();
builder.Configuration.GetSection(MongoOptions.SectionName).Bind(mongoOptions);

// Contexto Mongo
builder.Services.AddSingleton(new MongoContext(mongoOptions));

// Cloudinary Service NUEVO
builder.Services.AddCloudinaryService(builder.Configuration);


// Repositorio y servicios
builder.Services.AddScoped<IPublicationRepository, PublicationRepository>();
builder.Services.AddScoped<IPublicationService, PublicationService>();
builder.Services.AddScoped<IReportRepository, ReportRepository>();
builder.Services.AddScoped<IReportService, ReportService>();
builder.Services.AddScoped<IEmailRepository, EmailRepository>();
builder.Services.AddScoped<IEmailService, EmailService>();


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
app.UseCors(corsPolicy);
app.MapControllers();

app.Run();