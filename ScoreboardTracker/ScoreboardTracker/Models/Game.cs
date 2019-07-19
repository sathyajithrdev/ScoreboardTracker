using Newtonsoft.Json;
using Plugin.CloudFirestore.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;

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
                _scores = JsonConvert.DeserializeObject<List<UserScore>>(scoresJson);
            }
        }

        private string _scoresJson;

        private List<UserScore> _scores;

        public List<UserScore> getUserScores() {
            _scores = JsonConvert.DeserializeObject<List<UserScore>>(scoresJson);
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
            int indexToUpdate = _scores.FindIndex(s => s.userId == userScore.userId);
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
            list.ForEach(l => addUserScore(l));
        }

        public void setUserScores(List<UserScore> list)
        {
            _scores = list;
            scoresJson = JsonConvert.SerializeObject(_scores);
        }
    }
}
