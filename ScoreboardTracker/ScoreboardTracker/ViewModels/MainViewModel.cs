using Plugin.CloudFirestore;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Input;
using Xamarin.Forms;

using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Models;

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

        public async Task initGroupAndUsers()
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

                await populateCurrentGame();


                if (currentGame != null && listener != null)
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
                    currentGame.scores.Add(new UserScore()
                    {
                        user = u,
                        scores = new List<int?>(7) { null, null, null, null, null, null, null }
                    });
                });
            }
        }

        public void setListener(IGameScoreHandlerListener listener)
        {
            this.listener = listener;
        }

        public async Task<Tuple<bool, string>> onStartGame()
        {
            try
            {
                initScoreValues();

                await CrossCloudFirestore.Current
                           .Instance
                           .GetCollection($"groups/{groupDocId}/games")
                           .AddDocumentAsync(currentGame);

                await populateCurrentGame();

                if (listener != null)
                {
                    listener.onUserScoresChanged();
                }
                return new Tuple<bool, string>(true, "");
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                page.ShowToast(ex.Message);
                return new Tuple<bool, string>(false, "");
            }
        }

        private async Task populateCurrentGame()
        {
            var gameDocQuery = await CrossCloudFirestore.Current
                                         .Instance
                                         .GetCollection($"groups/{groupDocId}/games")
                                         .WhereEqualsTo("isCompleted", false)
                                         .GetDocumentsAsync();

            var game = gameDocQuery?.ToObjects<Game>()?.FirstOrDefault();
            if (game != null)
            {
                game.scores.ForEach(s =>
                {
                    s.user = Users.FirstOrDefault(u => u.userId == s.userId);
                });
                currentGame = game;
            }
        }

        public async Task<Tuple<bool, string>> onEndGame(Game game)
        {
            if (game.scores.All(u => u.scores.Count(s => s.HasValue) == 7))
            {
                currentGame.isCompleted = true;

                try
                {
                    var winner = currentGame.scores.Aggregate((curMin, s) => curMin == null || (s.scores.Sum() ?? 0) <
                                                                             curMin.scores.Sum() ? s : curMin);

                    var looser = currentGame.scores.Aggregate((curMax, s) => curMax == null || (s.scores.Sum() ?? 0) >
                                                                             curMax.scores.Sum() ? s : curMax);

                    currentGame.winnerId = winner.userId;
                    currentGame.looserId = looser.userId;

                    await CrossCloudFirestore.Current
                                .Instance
                                .GetCollection($"groups/{groupDocId}/games")
                                .GetDocument(currentGame.gameId)
                                .UpdateDataAsync(currentGame);

                    currentGame = null;

                    MessagingCenter.Send(this, "gameCompleted");

                    return new Tuple<bool, string>(true, $"Match won by {winner?.user?.name}");
                }
                catch (Exception ex)
                {
                    await page.DisplayAlert(ex.Message);
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
            if (currentGame == null)
            {
                return;
            }
            try
            {
                currentGame.scores = game.scores;

                await CrossCloudFirestore.Current
                                    .Instance
                                    .GetCollection($"groups/{groupDocId}/games")
                                    .GetDocument(currentGame.gameId)
                                    .UpdateDataAsync((currentGame));

                if (game.scores.All(u => u.scores.Count(s => s.HasValue) == 6))
                {
                    if (listener != null)
                    {
                        List<Tuple<UserScore, int>> userTotalScores = game.scores.Select(u => new Tuple<UserScore, int>(u, u.scores.Sum() ?? 0)).ToList();

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
