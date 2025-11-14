using Explorify.Api.Publications.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Application.Interfaces
{
    public interface IEmailVerificationRepository
    {
        Task<ZeroBounceResponse> VerifyEmail(string email, string apiKey);
    }
}
