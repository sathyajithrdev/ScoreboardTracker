using Plugin.CloudFirestore;
using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.Windows.Input;
using Xamarin.Forms;

namespace ScoreboardTracker.ViewModels
{
    public class MainViewModel : BaseViewModel, IGameScoreHandler
    {
        public ObservableCollection<User> Users { get; private set; }
        public ObservableCollection<UserScore> UsersScore { get; private set; }

        public ICommand LoadUsersCommand => new Command(() => initGroupAndUsers());

        private Action UserListListener { get; set; }
        public ILastGameReachedListener LastSetListener { get; set; }

        private string groupDocId;

        private Game currentGame;


        public MainViewModel()
        {
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

                var users = allUsers.Where(u => group.userIds.Contains(u.userId));

                var currentGameQuery = await CrossCloudFirestore.Current
                             .Instance
                             .GetCollection($"groups/{groupDocId}/games")
                             .WhereEqualsTo("isCompleted", false)
                             .GetDocumentsAsync();

                currentGame = currentGameQuery.ToObjects<Game>().FirstOrDefault();

                users.ToList().ForEach(u =>
                {
                    u.userScore = currentGame.getUserScores().FirstOrDefault(c => c.userId == u.userId);
                    Users.Add(u);
                });

                OnPropertyChanged(nameof(Users));
                IsBusy = false;
                if (UserListListener != null)
                {
                    UserListListener.Invoke();
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                IsBusy = false;
            }
        }

        public void setUserListChangedListener(Action action)
        {
            UserListListener = action;
        }

        public async void onStartGame()
        {
            Game game = new Game();
            game.gameId = Guid.NewGuid().ToString();
            game.addUserScores(Users.Select(u => u.userScore).ToList());

            try
            {
                await CrossCloudFirestore.Current
                           .Instance
                           .GetCollection($"groups/{groupDocId}/games")
                           .AddDocumentAsync(game);

                var gameDocQuery = await CrossCloudFirestore.Current
                             .Instance
                             .GetCollection($"groups/{groupDocId}/games")
                             .GetDocumentsAsync();

                var games = gameDocQuery.ToObjects<Game>();

                currentGame = games.LastOrDefault();

            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
            }

        }

        public void onGameCompletedListener()
        {
            throw new NotImplementedException();
        }

        public async void onScoreChangedListener(List<UserScore> userScore)
        {
            if (currentGame == null)
            {
                return;
            }

            currentGame.setUserScores(userScore);

            await CrossCloudFirestore.Current
                                .Instance
                                .GetCollection($"groups/{groupDocId}/games")
                                .GetDocument(currentGame.gameId)
                                .UpdateDataAsync(currentGame);

            if (userScore.All(u => u.scores.Count(s => s.HasValue) == 6))
            {
                if (LastSetListener != null)
                {
                    List<Tuple<UserScore, int>> userTotalScores = userScore.Select(u => new Tuple<UserScore, int>(u, u.scores.Sum() ?? 0)).ToList();

                    int maxScore = userTotalScores.Max(u => u.Item2);
                    Tuple<UserScore, int> maxScoredUser = userTotalScores.FirstOrDefault(u => u.Item2 == maxScore);
                    userTotalScores.Remove(maxScoredUser);

                    int secondMaxScore = userTotalScores.Max(u => u.Item2);
                    Tuple<UserScore, int> secondMaxScoredUser = userTotalScores.FirstOrDefault(u => u.Item2 == secondMaxScore);

                    int scoreToStand = (maxScoredUser.Item2 - secondMaxScoredUser.Item2) / 2;

                    LastSetListener.onLastGameReached(maxScoredUser.Item1, secondMaxScoredUser.Item1, $"{maxScoredUser.Item1.userId} will stand at {scoreToStand} against {secondMaxScoredUser.Item1.userId}");

                }
            }

        }
    }
}
