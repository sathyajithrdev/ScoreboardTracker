using System.Collections.Generic;

namespace ScoreboardTracker.Models
{
    public class UserScore
    {
        public string userId { get; set; }

        public List<int?> scores { get; set; }

        public UserScore()
        {
        }
    }
}
