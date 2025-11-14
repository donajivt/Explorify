using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Explorify.Api.Publications.Domain.Entities
{
    public class ZeroBounceResponse
    {
        public string Address { get; set; }
        public string Status { get; set; }
        public string Sub_status { get; set; }
        public string Account { get; set; }
        public string Domain { get; set; }
        public string Error { get; set; }
    }
}
