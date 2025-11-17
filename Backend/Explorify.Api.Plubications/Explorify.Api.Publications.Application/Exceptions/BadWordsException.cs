using System;
using System.Collections.Generic;

namespace Explorify.Api.Publications.Application.Exceptions
{
    public class BadWordsException : Exception
    {
        public string Field { get; set; }
        public List<string> BadWords { get; set; }

        public BadWordsException(string field, List<string> badWords)
            : base($"El campo '{field}' contiene palabras inapropiadas")
        {
            Field = field;
            BadWords = badWords;
        }

        public BadWordsException(string field, List<string> badWords, string customMessage)
            : base(customMessage)
        {
            Field = field;
            BadWords = badWords;
        }
    }
}