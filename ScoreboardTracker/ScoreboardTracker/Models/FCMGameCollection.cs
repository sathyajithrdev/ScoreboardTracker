using Newtonsoft.Json;
using Plugin.CloudFirestore.Attributes;

namespace ScoreboardTracker.Models
{
    public class FCMGameCollection
    {
        [Id]
        public string gameId { get; set; }

        public bool isCompleted { get; set; }

        public string scoresJson { get; set; }

        public FCMGameCollection()
        {
        }

        public FCMGameCollection(Game game)
        {
            this.gameId = game.gameId;
            this.isCompleted = game.isCompleted;
            this.scoresJson = JsonConvert.SerializeObject(game.scores, new JsonSerializerSettings()
            {
                PreserveReferencesHandling = PreserveReferencesHandling.None
            });
        }
    }
}
