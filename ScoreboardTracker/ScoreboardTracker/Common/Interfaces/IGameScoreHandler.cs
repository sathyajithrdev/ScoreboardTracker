using ScoreboardTracker.Models;
using System;
using System.Threading.Tasks;

namespace ScoreboardTracker.Common.Interfaces
{
    public interface IGameScoreHandler
    {
        Task<Tuple<bool, string>> onStartGame();

        Task<Tuple<bool, string>> onEndGame(Game game);

        void setListener(IGameScoreHandlerListener listener);

        void onScoreChangedListener(Game game);
    }


    public interface IGameScoreHandlerListener
    {
        void onUserScoresChanged();

        void onLastSetReached(UserScore firstUser, UserScore secondUser, string message);
    }

}
