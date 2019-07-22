using Plugin.CloudFirestore.Attributes;
using System.Collections.Generic;

namespace ScoreboardTracker.Models
{
    public class UserScore
    {
        [Ignored]
        public User user { get; set; }

        public List<int?> scores { get; set; }

        public UserScore()
        {
        }
    }
}
