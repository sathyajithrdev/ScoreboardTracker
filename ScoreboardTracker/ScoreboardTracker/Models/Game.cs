using Newtonsoft.Json;
using Plugin.CloudFirestore.Attributes;
using System.Collections.Generic;

namespace ScoreboardTracker.Models
{
    public class Game
    {
        [Id]
        public string gameId { get; set; }
        public bool isCompleted { get; set; }
        public string scoresJson
        {
            get => JsonConvert.SerializeObject(scores);
            set
            {
                scores = JsonConvert.DeserializeObject<List<UserScore>>(value);
            }
        }

        public string winnerId { get; set; }
        public string looserId { get; set; }

        [JsonIgnore, Ignored]
        public List<UserScore> scores { get; set; }

        public Game()
        {
            scores = new List<UserScore>();
        }

        public override bool Equals(object obj)
        {
            if (obj == null || this.GetType() != obj.GetType())
            {
                return false;
            }

            var data = obj as Game;

            return gameId == data.gameId
                && isCompleted == data.isCompleted
                && winnerId == data.winnerId
                && looserId == data.looserId
                && scoresJson.Equals(data.scoresJson);
        }

        public override int GetHashCode()
        {
            var hashCode = 1508867953;
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(gameId);
            hashCode = hashCode * -1521134295 + isCompleted.GetHashCode();
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(scoresJson);
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(winnerId);
            hashCode = hashCode * -1521134295 + EqualityComparer<string>.Default.GetHashCode(looserId);
            hashCode = hashCode * -1521134295 + EqualityComparer<List<UserScore>>.Default.GetHashCode(scores);
            return hashCode;
        }
    }
}
