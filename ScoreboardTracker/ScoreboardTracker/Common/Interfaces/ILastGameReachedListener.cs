using ScoreboardTracker.Models;

namespace ScoreboardTracker.Common.Interfaces
{
    public interface ILastGameReachedListener
    {
        void onLastGameReached(UserScore firstUser, UserScore secondUser, string message);
    }
}
