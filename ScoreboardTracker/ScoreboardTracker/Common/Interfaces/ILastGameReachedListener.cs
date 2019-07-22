using ScoreboardTracker.Models;

namespace ScoreboardTracker.Common.Interfaces
{
    public interface ILastSetReachedListener
    {
        void onLastSetReached(UserScore firstUser, UserScore secondUser, string message);
    }
}
