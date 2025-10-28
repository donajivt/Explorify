# Dockerfile multi-stage para API de Publicaciones

# Etapa 1: Build - Construcción de la aplicación
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copiamos los .csproj para aprovechar caché
COPY ["Explorify.Api.Publications/Explorify.Api.Publications.csproj", "Explorify.Api.Publications/"]
COPY ["Explorify.Api.Publications.Application/Explorify.Api.Publications.Application.csproj", "Explorify.Api.Publications.Application/"]
COPY ["Explorify.Api.Publications.Domain/Explorify.Api.Publications.Domain.csproj", "Explorify.Api.Publications.Domain/"]
COPY ["Explorify.Api.Publications.Infrastructure/Explorify.Api.Publications.Infrastructure.csproj", "Explorify.Api.Publications.Infrastructure/"]

# Restauramos dependencias
RUN dotnet restore "Explorify.Api.Publications/Explorify.Api.Publications.csproj"

# Copiamos el resto del código
COPY . .

# Compilar en Release y publicar
WORKDIR "/src/Explorify.Api.Publications"
RUN dotnet build -c Release -o /app/build
RUN dotnet publish -c Release -o /app/publish /p:UseAppHost=false

# Etapa 2: Runtime - Contenedor final
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

# Crear usuario no-root (seguridad)
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiamos la publicación hecha en la etapa build
COPY --from=build /app/publish .

# Asignar permisos y cambiar usuario
RUN chown -R appuser:appuser /app
USER appuser

# Exponer puerto de la API
EXPOSE 8080
ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

# Healthcheck 
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Arranque de la aplicación
ENTRYPOINT ["dotnet", "Explorify.Api.Publications.dll"]
