using System.Threading.Tasks;
using Explorify.Api.Publications.Application.Dtos;
using Explorify.Api.Publications.Application.Interfaces;
using Explorify.Api.Publications.Domain.Entities;

namespace Explorify.Api.Publications.Infraestructure.Services
{
    public class EmailService : IEmailService
    {
        private readonly IEmailRepository _emailRepository;

        public EmailService(IEmailRepository emailRepository)
        {
            _emailRepository = emailRepository;
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

            var email = new Email
            {
                To = emailDto.To,
                Subject = emailDto.Subject,
                Body = emailDto.Body
            };

            var sent = await _emailRepository.SendEmailAsync(email);

            if (sent)
            {
                response.IsSuccess = true;
                response.Message = "Correo enviado correctamente.";
                response.Result = emailDto;
            }
            else
            {
                response.IsSuccess = false;
                response.Message = "Error al enviar el correo.";
            }

            return response;
        }
    }
}
