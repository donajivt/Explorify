# ==============================================
# Dockerfile multi-stage para Explorify API - Publicaciones
# ==============================================

# ---------- Etapa 1: Build ----------
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copiar proyectos (.csproj) — rutas relativas al Dockerfile
COPY ["Explorify.Api.Plubications/Explorify.Api.Plubications.csproj", "Explorify.Api.Plubications/"]
COPY ["Explorify.Api.Plubications.Application/Explorify.Api.Plubications.Application.csproj", "Explorify.Api.Plubications.Application/"]
COPY ["Explorify.Api.Plubications.Domain/Explorify.Api.Plubications.Domain.csproj", "Explorify.Api.Plubications.Domain/"]
COPY ["Explorify.Api.Plubications.Infrastructure/Explorify.Api.Plubications.Infrastructure.csproj", "Explorify.Api.Plubications.Infrastructure/"]

# Restaurar dependencias
RUN dotnet restore "Explorify.Api.Plubications/Explorify.Api.Plubications.csproj"

# Copiar todo el contenido del backend (mismo nivel del Dockerfile)
COPY . .

# Establecer el directorio del proyecto principal
WORKDIR "/src/Explorify.Api.Plubications"

# Compilar y publicar
RUN dotnet build "Explorify.Api.Plubications.csproj" -c Release -o /app/build
RUN dotnet publish "Explorify.Api.Plubications.csproj" -c Release -o /app/publish /p:UseAppHost=false


# ---------- Etapa 2: Runtime ----------
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

# Crear usuario no-root (buenas prácticas)
RUN adduser --disabled-password --gecos "" appuser

# Copiar artefactos compilados
COPY --from=build /app/publish .

USER appuser

# Exponer puerto
EXPOSE 8080

# Variables de entorno
ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

# Healthcheck de ejemplo
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["dotnet", "Explorify.Api.Plubications.dll"]
