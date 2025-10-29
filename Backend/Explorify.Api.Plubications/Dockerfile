# Dockerfile multi-stage para Explorify API - Publicaciones
# Optimizado para .NET 9 en producción

# ========== Etapa 1: Build ========== #
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copiar proyectos por separado para uso eficiente del cache
COPY ["Explorify.Api.Plubications/Explorify.Api.Plubications.csproj", "Explorify.Api.Plubications/"]
COPY ["Explorify.Application/Explorify.Application.csproj", "Explorify.Application/"]
COPY ["Explorify.Domain/Explorify.Domain.csproj", "Explorify.Domain/"]
COPY ["Explorify.Infrastructure/Explorify.Infrastructure.csproj", "Explorify.Infrastructure/"]

# Restaurar dependencias
RUN dotnet restore "Explorify.Api.Plubications/Explorify.Api.Plubications.csproj"

# Copiar todo el código
COPY . .

# Compilar y Publicar en Release
WORKDIR "/src/Explorify.Api.Plubications"
RUN dotnet build "Explorify.Api.Plubications.csproj" -c Release -o /app/build
RUN dotnet publish "Explorify.Api.Plubications.csproj" -c Release -o /app/publish /p:UseAppHost=false

# ========== Etapa 2: Runtime ========== #
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

# Crear usuario no-root
RUN adduser --disabled-password --gecos "" appuser

# Copiar artefactos publicados
COPY --from=build /app/publish .

USER appuser

# Exponer puerto de la API
EXPOSE 8080

# ASP.NET Core Env
ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

# Healthcheck de ejemplo (ajusta ruta si tu API la usa diferente)
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["dotnet", "Explorify.Api.Plubications.dll"]
