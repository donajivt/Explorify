using System.Threading.Tasks;
using Explorify.Api.Publications.Domain.Entities;

namespace Explorify.Api.Publications.Application.Interfaces
{
    public interface IEmailRepository
    {
        Task<bool> SendEmailAsync(Email email);
    }
}
