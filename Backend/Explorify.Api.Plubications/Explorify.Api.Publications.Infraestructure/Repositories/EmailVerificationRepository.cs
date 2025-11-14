using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using System.Net.Http.Json;

namespace Explorify.Api.Publications.Infraestructure.Repositories
{

    public class EmailVerificationRepository : IEmailVerificationRepository
    {
        private readonly HttpClient _http;

        public EmailVerificationRepository(HttpClient http)
        {
            _http = http;
        }

        public async Task<ZeroBounceResponse> VerifyEmail(string email, string apiKey)
        {
            string url = $"https://api.zerobounce.net/v2/validate?api_key={apiKey}&email={email}";

            var response = await _http.GetAsync(url);

            if (!response.IsSuccessStatusCode)
            {
                return new ZeroBounceResponse
                {
                    Error = "ZeroBounce API error: " + response.StatusCode
                };
            }

            return await response.Content.ReadFromJsonAsync<ZeroBounceResponse>();
        }
    }

}
