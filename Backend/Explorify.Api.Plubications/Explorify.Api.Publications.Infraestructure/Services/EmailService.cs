using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;
using System.Net.Mail;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Infraestructure.Services
{
    public class EmailService : IEmailService
    {
        private readonly IEmailRepository _emailRepository;
        private readonly IEmailVerificationService _emailValidationService;

        public EmailService(IEmailRepository emailRepository, IEmailVerificationService emailValidationService)
        {
            _emailRepository = emailRepository;

            _emailValidationService = emailValidationService;
            _emailValidationService = emailValidationService;
        }

        public async Task<ResponseDto> SendEmailAsync(EmailDto emailDto)
        {
            var response = new ResponseDto();

            if (string.IsNullOrWhiteSpace(emailDto.To) ||
                string.IsNullOrWhiteSpace(emailDto.Subject) ||
                string.IsNullOrWhiteSpace(emailDto.Body))
            {
                response.IsSuccess = false;
                response.Message = "Debe proporcionar destinatario, asunto y cuerpo del correo.";
                return response;
            }

            // Validar formato
            if (!IsValidEmail(emailDto.To))
            {
                response.IsSuccess = false;
                response.Message = "El correo del destinatario no es válido.";
                return response;
            }
            var validation = await _emailValidationService.VerifyEmail(emailDto.To);

            if (validation.Status == "invalid" || validation.Status == "unknow")
            {
                response.IsSuccess = false;
                response.Message = $"Correo inválido según ZeroBounce: {validation.SubStatus}";
                return response;
            }

            var email = new Email
            {
                To = emailDto.To,
                Subject = emailDto.Subject,
                Body = emailDto.Body
            };

            var result = await _emailRepository.SendEmailAsync(email);

            if (!result.Success)
            {
                response.IsSuccess = false;
                response.Message = result.ErrorMessage;
                return response;
            }

            response.IsSuccess = true;
            response.Message = "Correo enviado correctamente.";
            response.Result = emailDto;
            return response;
        }
        private bool IsValidEmail(string email)
        {
            try
            {
                var mail = new MailAddress(email);
                return true;
            }
            catch
            {
                return false;
            }
        }

    }
}
