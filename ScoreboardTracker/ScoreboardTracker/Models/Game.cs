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

        //private string _scoresJson;

        public Game()
        {
            scores = new List<UserScore>();
        }

        //public Game(FCMGameCollection game)
        //{
        //    this.isCompleted = game.isCompleted;
        //    this.gameId = game.gameId;
        //    this.scores = JsonConvert.DeserializeObject<List<UserScore>>(game.scoresJson);
        //}

        //public List<UserScore> getUserScores()
        //{
        //    return scores;
        //}

        //public void addUserScore(UserScore userScore)
        //{
        //    if (scores == null)
        //    {
        //        scores = new List<UserScore>();
        //    }

        //    scores.Add(userScore);
        //    //scoresJson = JsonConvert.SerializeObject(_scores);
        //}

        //public void removeUserScore(UserScore userScore)
        //{
        //    if (scores == null)
        //    {
        //        return;
        //    }
        //    scores.Remove(userScore);
        //}

        //public void updateOrInsertUserScore(UserScore userScore)
        //{
        //    if (scores == null)
        //    {
        //        scores = new List<UserScore>();
        //    }
        //    int indexToUpdate = scores.FindIndex(s => s.user.userId == userScore.user.userId);
        //    if (indexToUpdate >= 0)
        //    {
        //        scores[indexToUpdate] = userScore;
        //    }
        //    else
        //    {
        //        scores.Add(userScore);
        //    }
        //    //scoresJson = JsonConvert.SerializeObject(_scores);
        //}

        //public void addUserScores(List<UserScore> list)
        //{
        //    if (scores == null)
        //    {
        //        scores = new List<UserScore>();
        //    }
        //    scores.AddRange(list);
        //    //scoresJson = JsonConvert.SerializeObject(_scores);
        //}

        //public void setUserScoresJson(List<UserScore> list)
        //{
        //    scores = null;
        //    addUserScores(list);
        //}
    }
}
