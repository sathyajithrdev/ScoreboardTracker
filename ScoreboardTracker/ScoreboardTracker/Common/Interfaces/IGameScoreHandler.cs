using ScoreboardTracker.Models;
using System;
using System.Collections.Generic;
using System.Text;

namespace ScoreboardTracker.Common.Interfaces
{
    public interface IGameScoreHandler
    {

        void onStartGame();

        void onGameCompletedListener();

        void onScoreChangedListener(List<UserScore> userScore);

        void setUserListChangedListener(Action action);
    }
}
