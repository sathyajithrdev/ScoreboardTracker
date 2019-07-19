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

                List<User> enumerable = usersQuery.ToObjects<User>().ToList();
                List<User> users = enumerable.Where(u => group.userIds.Contains(u.userId)).ToList();

                users.ForEach(u =>
                {
                    u.userScore = new UserScore() { userId = u.userId, scores = new List<int?>(7) };
                    for (int i = 0; i <= 6; i++)
                        u.userScore.scores.Add(null);
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
            currentGame.setUserScores(userScore);

            await CrossCloudFirestore.Current
                                .Instance
                                .GetCollection($"groups/{groupDocId}/games")
                                .GetDocument(currentGame.gameId)
                                .UpdateDataAsync(currentGame);

        }
    }
}
