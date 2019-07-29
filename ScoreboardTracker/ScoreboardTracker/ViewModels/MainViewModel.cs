using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Input;
using Xamarin.Forms;

using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Models;
using ScoreboardTracker.Services;

namespace ScoreboardTracker.ViewModels
{
    public class MainViewModel : BaseViewModel, IGameScoreHandler, IGameScoreChangeListener
    {
        private List<User> Users { get; set; }

        public ICommand LoadUsersCommand => new Command(async () => await initGroupAndUsers());

        private IGameScoreHandlerListener listener { get; set; }

        private string _groupDocId;
        private readonly IPage _page;
        private readonly IScoreboardRepository _scoreboardRepository;

        public Game CurrentGame;

        public string WinnerId { get; private set; }

        public MainViewModel(IPage page, IScoreboardRepository scoreboardRepository)
        {
            _page = page;
            _scoreboardRepository = scoreboardRepository;
        }

        public async Task initGroupAndUsers()
        {
            Users = new List<User>();

            try
            {
                if (IsBusy)
                {
                    return;
                }
                IsBusy = true;

                var group = await _scoreboardRepository.GetGroup();

                if (group == null)
                {
                    IsBusy = false;
                    return;
                }

                _groupDocId = group.groupId;

                var allUsers = await _scoreboardRepository.GetAllUsers();

                Users = allUsers?.Where(u => group.userIds.Contains(u.userId)).ToList();

                await populateOnGoingGame();

                await populatePlayerDetails();

                if (CurrentGame != null)
                {
                    listener?.onUserScoresChanged();
                }
                initGameScoreChangeListener();
                //(_scoreboardRepository as ScoreboardRepository).AddDummyDataGroup(_groupDocId);
                IsBusy = false;
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                _page.ShowToast(ex.Message);
                IsBusy = false;
            }
        }

        private void initGameScoreChangeListener()
        {
            _scoreboardRepository.StartGameScoreListener(_groupDocId, this);
        }

        private void initScoreValues()
        {
            if (CurrentGame == null)
            {
                CurrentGame = new Game();
                Users.ToList().ForEach(u =>
                {
                    CurrentGame.scores.Add(new UserScore()
                    {
                        user = u,
                        scores = new List<int?>(7) { null, null, null, null, null, null, null }
                    });
                });
            }
        }

        public void setListener(IGameScoreHandlerListener gameScoreHandlerListener)
        {
            listener = gameScoreHandlerListener;
        }

        public async Task<Tuple<bool, string>> onStartGame()
        {
            try
            {
                initScoreValues();

                await _scoreboardRepository.AddGame(_groupDocId, CurrentGame);

                await populateOnGoingGame();
                listener?.onUserScoresChanged();
                return new Tuple<bool, string>(true, "");
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                _page.ShowToast(ex.Message);
                return new Tuple<bool, string>(false, "");
            }
        }

        private async Task populateOnGoingGame()
        {
            var onGoingGame = await _scoreboardRepository.GetOnGoingGame(_groupDocId);

            if (onGoingGame != null)
            {
                onGoingGame.scores.ForEach(s =>
                {
                    s.user = Users.FirstOrDefault(u => u.userId == s.userId);
                });
                CurrentGame = onGoingGame;
            }
        }

        public async Task populatePlayerDetails()
        {
            var allGames = await _scoreboardRepository.GetCompletedGames(_groupDocId);

            if (allGames != null)
            {
                var playerWinStats = allGames.GroupBy(g => g.winnerId).Select(p => new { id = p.Key, wins = p.Count() });
                var playerLossStats = allGames.GroupBy(g => g.looserId).Select(p => new { id = p.Key, wins = p.Count() });

                Users.ForEach(u =>
                {
                    u.winCount = playerWinStats.FirstOrDefault(p => p.id == u.userId)?.wins ?? 0;
                    u.lossCount = playerLossStats.FirstOrDefault(p => p.id == u.userId)?.wins ?? 0;
                });

                if (CurrentGame != null)
                {
                    CurrentGame.scores.ForEach(s =>
                    {
                        s.user = Users.FirstOrDefault(u => u.userId == s.userId);
                    });

                    listener?.onUserScoresChanged();
                }
            }
        }

        public async Task<Tuple<bool, string>> onEndGame(Game game)
        {
            if (game.scores.All(u => u.scores.Count(s => s.HasValue) == 7))
            {
                CurrentGame.isCompleted = true;

                try
                {
                    var winner = CurrentGame.scores.Aggregate((curMin, s) => curMin == null || (s.scores.Sum() ?? 0) <
                                                                             curMin.scores.Sum() ? s : curMin);

                    var looser = CurrentGame.scores.Aggregate((curMax, s) => curMax == null || (s.scores.Sum() ?? 0) >
                                                                             curMax.scores.Sum() ? s : curMax);

                    CurrentGame.winnerId = winner.userId;
                    WinnerId = winner.userId;
                    CurrentGame.looserId = looser.userId;

                    await _scoreboardRepository.UpdateGame(_groupDocId, CurrentGame);

                    MessagingCenter.Send(this, "gameCompleted");

                    return new Tuple<bool, string>(true, $"Match won by {winner.user?.name}");
                }
                catch (Exception ex)
                {
                    await _page.DisplayAlert(ex.Message);
                    return new Tuple<bool, string>(false, ex.Message);
                }
            }
            else
            {
                return new Tuple<bool, string>(false, "Please enter scores for all sets");
            }
        }

        public async void onScoreChangedListener(Game game)
        {
            if (CurrentGame == null)
            {
                return;
            }
            try
            {
                CurrentGame.scores = game.scores;

                await _scoreboardRepository.UpdateGame(_groupDocId, CurrentGame);

                List<Tuple<UserScore, int>> userTotalScores = game.scores.Select(u => new Tuple<UserScore, int>(u, u.scores.Sum() ?? 0)).ToList();
                if (game.scores.All(u => u.scores.Count(s => s.HasValue) == 6))
                {
                    if (listener == null) return;

                    var minScore = userTotalScores.Min(u => u.Item2);
                    var minScoredUser = userTotalScores.FirstOrDefault(u => u.Item2 == minScore);
                    userTotalScores.Remove(minScoredUser);

                    var secondMinScore = userTotalScores.Min(u => u.Item2);
                    var secondMinScoredUser = userTotalScores.FirstOrDefault(u => u.Item2 == secondMinScore);

                    if (minScoredUser == null || secondMinScoredUser == null) return;

                    var scoreToStand = (secondMinScoredUser.Item2 - 1 - minScoredUser.Item2) / 2;
                    if (scoreToStand < 0)
                    {
                        scoreToStand = 0;
                    }

                    listener.onLastSetReached(minScoredUser.Item1, secondMinScoredUser.Item1, $"{minScoredUser.Item1.user.name} will stand at {scoreToStand} against {secondMinScoredUser.Item1.user.name}");
                }
                else
                {
                    listener.onLastSetReached(null, null, null);
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                _page.ShowToast(ex.Message);
            }
        }

        public void gameScoreUpdated()
        {
            Task.Run(async () =>
            {
                await populateOnGoingGame();
                listener?.onUserScoresChanged();
            });
        }

        public void newGameCreated()
        {
            Task.Run(async () =>
            {
                await populateOnGoingGame();
                await populatePlayerDetails();
            });
        }
    }
}
