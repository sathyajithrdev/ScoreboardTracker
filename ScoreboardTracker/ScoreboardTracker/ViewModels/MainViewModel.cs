using Plugin.CloudFirestore;
using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Input;
using Xamarin.Forms;

namespace ScoreboardTracker.ViewModels
{
    public class MainViewModel : BaseViewModel, IGameScoreHandler
    {
        private ObservableCollection<User> Users { get; set; }
        private ObservableCollection<UserScore> UsersScore { get; set; }

        public ICommand LoadUsersCommand => new Command(() => initGroupAndUsers());

        private IGameScoreHandlerListener listener { get; set; }

        private string groupDocId;

        public Game currentGame;

        private IPage page;


        public MainViewModel(IPage page)
        {
            this.page = page;
        }

        private async void initGroupAndUsers()
        {
            Users = new ObservableCollection<User>();
            UsersScore = new ObservableCollection<UserScore>();

            try
            {
                if (IsBusy)
                {
                    return;
                }
                IsBusy = true;
                var groupQuery = await CrossCloudFirestore.Current
                        .Instance
                        .GetCollection("groups")
                        .LimitTo(1)
                        .GetDocumentsAsync();

                groupDocId = groupQuery.Documents.FirstOrDefault().Id;

                Group group = groupQuery.ToObjects<Group>().FirstOrDefault();

                var usersQuery = await CrossCloudFirestore.Current
                        .Instance
                        .GetCollection("users")
                        .GetDocumentsAsync();

                List<User> allUsers = usersQuery.ToObjects<User>().ToList();

                Users = new ObservableCollection<User>(allUsers.Where(u => group.userIds.Contains(u.userId)).ToList());
                OnPropertyChanged(nameof(Users));

                var currentGameQuery = await CrossCloudFirestore.Current
                             .Instance
                             .GetCollection($"groups/{groupDocId}/games")
                             .WhereEqualsTo("isCompleted", false)
                             .GetDocumentsAsync();

                currentGame = currentGameQuery.ToObjects<Game>().FirstOrDefault();

                initScoreValues();
                if (listener != null)
                {
                    listener.onUserScoresChanged();
                }
                IsBusy = false;
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                page.ShowToast(ex.Message);
                IsBusy = false;
            }
        }

        private void initScoreValues()
        {
            if (currentGame == null)
            {
                currentGame = new Game();
                Users.ToList().ForEach(u =>
                {
                    currentGame._scores.Add(new UserScore()
                    {
                        user = u,
                        scores = new List<int?>(7) { null, null, null, null, null, null, null }
                    });
                });
            }
            Users.ToList().ForEach(u =>
            {
                u.userScore = currentGame._scores.FirstOrDefault(c => c.user.userId == u.userId);
                u.userScore.user = u;
            });
        }

        public void setListener(IGameScoreHandlerListener listener)
        {
            this.listener = listener;
        }

        public async Task<bool> onStartGame()
        {
            try
            {
                initScoreValues();
                await CrossCloudFirestore.Current
                           .Instance
                           .GetCollection($"groups/{groupDocId}/games")
                           .AddDocumentAsync(currentGame);

                var gameDocQuery = await CrossCloudFirestore.Current
                             .Instance
                             .GetCollection($"groups/{groupDocId}/games")
                             .GetDocumentsAsync();

                var games = gameDocQuery?.ToObjects<Game>();
                currentGame = games?.LastOrDefault();
                if (listener != null)
                {
                    listener.onUserScoresChanged();
                }
                return true;
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                page.ShowToast(ex.Message);
            }
            return false;
        }

        public async Task<bool> onEndGame(Game game)
        {
            if (game.getUserScores().All(u => u.scores.Count(s => s.HasValue) == 7))
            {
                currentGame.isCompleted = true;

                await CrossCloudFirestore.Current
                                .Instance
                                .GetCollection($"groups/{groupDocId}/games")
                                .GetDocument(currentGame.gameId)
                                .UpdateDataAsync(currentGame);

                return true;
            }
            else
            {
                await page.DisplayAlert("Please enter scores for all sets");
                return false;
            }

        }

        public void onGameCompletedListener()
        {
            throw new NotImplementedException();
        }

        public async void onScoreChangedListener(Game game)
        {
            if (currentGame == null)
            {
                return;
            }
            try
            {
                currentGame.setUserScoresJson(game._scores);

                await CrossCloudFirestore.Current
                                    .Instance
                                    .GetCollection($"groups/{groupDocId}/games")
                                    .GetDocument(currentGame.gameId)
                                    .UpdateDataAsync(currentGame);

                if (game.getUserScores().All(u => u.scores.Count(s => s.HasValue) == 6))
                {
                    if (listener != null)
                    {
                        List<Tuple<UserScore, int>> userTotalScores = game.getUserScores().Select(u => new Tuple<UserScore, int>(u, u.scores.Sum() ?? 0)).ToList();

                        int minScore = userTotalScores.Min(u => u.Item2);
                        Tuple<UserScore, int> minScoredUser = userTotalScores.FirstOrDefault(u => u.Item2 == minScore);
                        userTotalScores.Remove(minScoredUser);

                        int secondMinScore = userTotalScores.Min(u => u.Item2);
                        Tuple<UserScore, int> secondMinScoredUser = userTotalScores.FirstOrDefault(u => u.Item2 == secondMinScore);

                        int scoreToStand = (secondMinScoredUser.Item2 - 1 - minScoredUser.Item2) / 2;
                        if (scoreToStand < 0)
                        {
                            scoreToStand = 0;
                        }

                        listener.onLastSetReached(minScoredUser.Item1, secondMinScoredUser.Item1, $"{minScoredUser.Item1.user.name} will stand at {scoreToStand} against {secondMinScoredUser.Item1.user.name}");

                    }
                }
                else
                {
                    listener.onLastSetReached(null, null, null);
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                page.ShowToast(ex.Message);
            }
        }
    }
}
