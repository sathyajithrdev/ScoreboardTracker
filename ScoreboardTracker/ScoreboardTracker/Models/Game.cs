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
            get => _scoresJson;
            set
            {
                _scoresJson = value;
                _scores = JsonConvert.DeserializeObject<List<UserScore>>(_scoresJson);
            }
        }

        private string _scoresJson;

        [Ignored]
        public List<UserScore> _scores { get; set; }

        public List<UserScore> getUserScores()
        {
            return _scores;
        }

        public void addUserScore(UserScore userScore)
        {
            if (_scores == null)
            {
                _scores = new List<UserScore>();
            }

            _scores.Add(userScore);
            scoresJson = JsonConvert.SerializeObject(_scores);
        }

        public void removeUserScore(UserScore userScore)
        {
            if (_scores == null)
            {
                return;
            }
            _scores.Remove(userScore);
        }

        public void updateOrInsertUserScore(UserScore userScore)
        {
            if (_scores == null)
            {
                _scores = new List<UserScore>();
            }
            int indexToUpdate = _scores.FindIndex(s => s.user.userId == userScore.user.userId);
            if (indexToUpdate >= 0)
            {
                _scores[indexToUpdate] = userScore;
            }
            else
            {
                _scores.Add(userScore);
            }
            scoresJson = JsonConvert.SerializeObject(_scores);
        }

        public void addUserScores(List<UserScore> list)
        {
            if (_scores == null)
            {
                _scores = new List<UserScore>();
            }
            _scores.AddRange(list);
            scoresJson = JsonConvert.SerializeObject(_scores);
        }

        public void setUserScoresJson(List<UserScore> list)
        {
            _scores = null;
            addUserScores(list);
        }
    }
}
