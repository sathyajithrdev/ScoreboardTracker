using Plugin.CloudFirestore.Attributes;

namespace ScoreboardTracker.Models
{
    public class User
    {
        public string userId { get; set; }

        public string name { get; set; }

        public string profileUrl { get; set; }

        [Ignored]
        public int winCount { get; set; }

        [Ignored]
        public int lossCount { get; set; }

        //Default constructor required for firestore
        public User()
        {
        }
    }
}
