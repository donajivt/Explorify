using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Logueo.Infrastructure.Persistence
{
    public class MongoOptions
    {
        public const string SectionName = "Mongo";
        public string ConnectionString { get; set; } = "";
        public string Database { get; set; } = "";
        public string PublicationsCollection { get; set; } = "publications";
    }
}
