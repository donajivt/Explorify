using System.IdentityModel.Tokens.Jwt;
using System.Net.Http.Headers;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using Newtonsoft.Json;

namespace Explorify.Api.Publications.IntegrationTests.Helpers
{
    public static class IntegrationTestHelpers
    {
        public static string GenerateJwtToken(string userId, string role = "User")
        {
            var secret = "JKDJKADHKDKJDODEWEJUWDI=SAM?NSHSAJKSAJKSAHKSAKJSA";
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secret));
            var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, userId),
                new Claim(ClaimTypes.Name, $"Test User {userId}"),
                new Claim(ClaimTypes.Role, role),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
            };

            var token = new JwtSecurityToken(
                issuer: "test-issuer",
                audience: "test-audience",
                claims: claims,
                expires: DateTime.UtcNow.AddHours(1),
                signingCredentials: credentials
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }

        public static void AddAuthorizationHeader(this HttpClient client, string userId, string role = "User")
        {
            var token = GenerateJwtToken(userId, role);
            client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        }

        public static StringContent CreateJsonContent(object data)
        {
            var json = JsonConvert.SerializeObject(data);
            return new StringContent(json, Encoding.UTF8, "application/json");
        }

        public static async Task<T?> DeserializeResponse<T>(HttpResponseMessage response)
        {
            var content = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<T>(content);
        }

        public static MultipartFormDataContent CreateMultipartContent(
            Dictionary<string, string> formData,
            string? fileFieldName = null,
            byte[]? fileContent = null,
            string? fileName = null)
        {
            var content = new MultipartFormDataContent();

            foreach (var item in formData)
            {
                content.Add(new StringContent(item.Value), item.Key);
            }

            if (fileFieldName != null && fileContent != null && fileName != null)
            {
                var fileStreamContent = new ByteArrayContent(fileContent);
                fileStreamContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
                content.Add(fileStreamContent, fileFieldName, fileName);
            }

            return content;
        }

        public static byte[] CreateFakeImageBytes()
        {
            // Crear un pequeño array de bytes que simula una imagen JPEG
            return new byte[] { 0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46 };
        }
    }
}