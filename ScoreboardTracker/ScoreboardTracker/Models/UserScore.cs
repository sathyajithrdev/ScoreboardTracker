using Newtonsoft.Json;
using Plugin.CloudFirestore.Attributes;
using System.Collections.Generic;

namespace ScoreboardTracker.Models
{
    public class UserScore
    {
        public string userId
        {
            get => user?.userId;
            set
            {
                if (value != user?.userId)
                {
                    user = new User() { userId = value };
                }
            }
        }

        public List<int?> scores { get; set; }

        [Ignored, JsonIgnore]
        public User user { get; set; }

        public UserScore()
        {
        }
    }
}
