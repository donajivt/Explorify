using System.Threading.Tasks;
using Explorify.Api.Publications.Application.Dtos;

namespace Explorify.Api.Publications.Application.Interfaces
{
    public interface IEmailService
    {
        Task<ResponseDto> SendEmailAsync(EmailDto email);
    }
}
