namespace ScoreboardTracker.Common.Interfaces
{
    public interface IGameScoreChangeListener
    {
        void gameScoreUpdated();

        void newGameCreated();
    }
}
