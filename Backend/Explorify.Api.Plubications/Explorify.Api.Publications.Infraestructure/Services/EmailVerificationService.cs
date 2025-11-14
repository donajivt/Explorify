using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using Microsoft.Extensions.Options;

public class EmailVerificationService : IEmailVerificationService
{
    private readonly IEmailVerificationRepository _repo;
    private readonly ZeroBounceSettings _settings;

    public EmailVerificationService(
        IEmailVerificationRepository repo,
        IOptions<ZeroBounceSettings> settings)
    {
        _repo = repo;
        _settings = settings.Value;
    }

    public async Task<EmailVerificationResponseDto> VerifyEmail(string email)
    {
        var result = await _repo.VerifyEmail(email, _settings.PrimaryKey);

        if (result == null ||
            !string.IsNullOrEmpty(result.Error) ||
            string.IsNullOrEmpty(result.Status))
        {
            result = await _repo.VerifyEmail(email, _settings.BackupKey);
        }

        if (result == null)
        {
            return new EmailVerificationResponseDto
            {
                Success = false,
                ErrorMessage = "No response from ZeroBounce"
            };
        }

        return new EmailVerificationResponseDto
        {
            Success = string.IsNullOrEmpty(result.Error),
            Status = result.Status,
            SubStatus = result.Sub_status,
            Account = result.Account,
            Domain = result.Domain,
            ErrorMessage = result.Error
        };
    }
}