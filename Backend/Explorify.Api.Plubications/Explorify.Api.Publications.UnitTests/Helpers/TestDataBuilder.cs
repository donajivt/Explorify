using Bogus;
using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Domain.Entities;
using MongoDB.Bson;

namespace Explorify.Api.Publications.UnitTests.Helpers
{
    public static class TestDataBuilder
    {
        private static readonly Faker _faker = new Faker("es");

        /// <summary>
        /// Genera un ObjectId válido de MongoDB
        /// </summary>
        private static string GenerateMongoId()
        {
            return ObjectId.GenerateNewId().ToString();
        }

        public static Publication CreatePublication(
            string? id = null,
            string? userId = null,
            string? title = null,
            string? description = null,
            string? location = null)
        {
            return new Publication
            {
                Id = id ?? GenerateMongoId(),
                UserId = userId ?? GenerateMongoId(),
                Title = title ?? _faker.Lorem.Sentence(3),
                Description = description ?? _faker.Lorem.Paragraph(),
                Location = location ?? _faker.Address.City(),
                ImageUrl = _faker.Image.PicsumUrl(),
                Latitud = _faker.Address.Latitude().ToString(),
                Longitud = _faker.Address.Longitude().ToString(),
                CreatedAt = DateTime.UtcNow
            };
        }

        public static PublicationDto CreatePublicationDto(
            string? id = null,
            string? userId = null,
            string? title = null,
            string? description = null)
        {
            return new PublicationDto
            {
                Id = id ?? GenerateMongoId(),
                UserId = userId,
                Title = title ?? _faker.Lorem.Sentence(3),
                Description = description ?? _faker.Lorem.Paragraph(),
                Location = _faker.Address.City(),
                ImageUrl = _faker.Image.PicsumUrl(),
                Latitud = _faker.Address.Latitude().ToString(),
                Longitud = _faker.Address.Longitude().ToString(),
                CreatedAt = DateTime.UtcNow
            };
        }

        public static List<Publication> CreatePublications(int count)
        {
            var publications = new List<Publication>();
            for (int i = 0; i < count; i++)
            {
                publications.Add(CreatePublication());
            }
            return publications;
        }

        public static PublicationReport CreateReport(
            string? publicationId = null,
            string? reportedByUserId = null)
        {
            return new PublicationReport
            {
                Id = GenerateMongoId(),
                PublicationId = publicationId ?? GenerateMongoId(),
                ReportedByUserId = reportedByUserId ?? GenerateMongoId(),
                Reason = _faker.PickRandom("Spam", "Contenido inapropiado", "Información falsa", "Acoso"),
                Description = _faker.Lorem.Sentence(),
                CreatedAt = DateTime.UtcNow
            };
        }

        public static EmailDto CreateEmailDto(
            string? to = null,
            string? subject = null,
            string? body = null)
        {
            return new EmailDto
            {
                To = to ?? _faker.Internet.Email(),
                Subject = subject ?? _faker.Lorem.Sentence(),
                Body = body ?? _faker.Lorem.Paragraph()
            };
        }

        public static ZeroBounceResponse CreateZeroBounceResponse(
            string status = "valid",
            string subStatus = "")
        {
            return new ZeroBounceResponse
            {
                Address = _faker.Internet.Email(),
                Status = status,
                Sub_status = subStatus,
                Account = _faker.Internet.UserName(),
                Domain = _faker.Internet.DomainName(),
                Error = string.Empty
            };
        }

        public static PublicationDto CreatePublicationDtoWithBadWords()
        {
            return new PublicationDto
            {
                Title = "Título con puto contenido",
                Description = "Descripción normal",
                Location = "Ciudad Normal",
                ImageUrl = _faker.Image.PicsumUrl(),
                Latitud = _faker.Address.Latitude().ToString(),
                Longitud = _faker.Address.Longitude().ToString()
            };
        }
    }
}