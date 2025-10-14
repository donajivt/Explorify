# Dockerfile multi-stage para API de Login
# Optimizado para .NET 9.0 y producción

# Etapa 1: Build - Construcción de la aplicación
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copiar archivos de proyecto y restaurar dependencias
# Se hace en pasos separados para aprovechar el caché de Docker
COPY ["Api.Login/Api.Login.csproj", "Api.Login/"]
COPY ["Logueo.Application/Logueo.Application.csproj", "Logueo.Application/"]
COPY ["Logueo.Domain/Logueo.Domain.csproj", "Logueo.Domain/"]
COPY ["Logueo.Infrastructure/Logueo.Infrastructure.csproj", "Logueo.Infrastructure/"]

# Restaurar dependencias NuGet
RUN dotnet restore "Api.Login/Api.Login.csproj"

# Copiar el resto del código fuente
COPY . .

# Construir y publicar la aplicación en modo Release
WORKDIR "/src/Api.Login"
RUN dotnet build "Api.Login.csproj" -c Release -o /app/build
RUN dotnet publish "Api.Login.csproj" -c Release -o /app/publish /p:UseAppHost=false

# Etapa 2: Runtime - Imagen final para ejecutar la aplicación
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

# Crear usuario no-root para mayor seguridad
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiar los archivos publicados desde la etapa de build
COPY --from=build /app/publish .

# Cambiar permisos y propietario
RUN chown -R appuser:appuser /app

# Cambiar a usuario no-root
USER appuser

# Exponer el puerto que usa la aplicación
EXPOSE 8080
EXPOSE 8081

# Variables de entorno para ASP.NET Core
ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production

# Healthcheck para verificar que la aplicación esté funcionando
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Punto de entrada de la aplicación
ENTRYPOINT ["dotnet", "Api.Login.dll"]
