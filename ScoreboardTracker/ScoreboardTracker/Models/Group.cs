using Plugin.CloudFirestore.Attributes;
using System.Collections.Generic;

namespace ScoreboardTracker.Models
{
    public class Group
    {
        [Id]
        public string groupId { get; set; }
        public string groupName { get; set; }
        public List<string> userIds { get; set; }
        public List<Game> games { get; set; }
    }
}
