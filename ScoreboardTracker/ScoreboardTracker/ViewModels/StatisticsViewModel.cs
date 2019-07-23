using Plugin.CloudFirestore;
using ScoreboardTracker.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Input;

using Xamarin.Forms;

namespace ScoreboardTracker.ViewModels
{
    public class StatisticsViewModel : BaseViewModel
    {
        private ObservableCollection<Statistics> _statistics;
        public ObservableCollection<Statistics> Statistics
        {
            get => _statistics;
            set
            {
                _statistics = value;
                OnPropertyChanged(nameof(Statistics));
            }
        }
        public StatisticsViewModel()
        {
            Title = "Statistics";
            OpenWebCommand = new Command(() => Device.OpenUri(new Uri("https://xamarin.com/platform")));
            populateStatisticsData();
            MessagingCenter.Subscribe<MainViewModel>(this, "gameCompleted", (sender) =>
            {
                populateStatisticsData();
            });
        }

        private async void populateStatisticsData()
        {
            var groupQuery = await CrossCloudFirestore.Current
                       .Instance
                       .GetCollection("groups")
                       .LimitTo(1)
                       .GetDocumentsAsync();

            var groupDocId = groupQuery.Documents.FirstOrDefault().Id;

            Group group = groupQuery.ToObjects<Group>().FirstOrDefault();

            var usersQuery = await CrossCloudFirestore.Current
                    .Instance
                    .GetCollection("users")
                    .GetDocumentsAsync();

            List<User> allUsers = usersQuery.ToObjects<User>().ToList();

            var Users = allUsers.Where(u => group.userIds.Contains(u.userId)).ToList();

            Statistics = new ObservableCollection<Statistics>();

            var gameDocQuery = await CrossCloudFirestore.Current
                       .Instance
                       .GetCollection($"groups/{groupDocId}/games")
                       .GetDocumentsAsync();

            var games = gameDocQuery?.ToObjects<Game>();
            if (games != null)
            {
                var completedGames = games.Where(g => g.isCompleted).ToList();

                var winningMatchCount = completedGames.GroupBy(g => g.winnerId)
                                                      .Select(w => new { userId = w.Key, noOfWins = w.Count() })
                                                        .Aggregate((currentMax, next) => currentMax == null || next.noOfWins > currentMax.noOfWins ? next : currentMax);

                var enumerable = completedGames.GroupBy(g => g.looserId).Select(w => new { userId = w.Key, noOfLoss = w.Count() }).ToList();
                var lossMatchCount = enumerable
                     .Aggregate((currentMin, next) => currentMin == null || next.noOfLoss < currentMin.noOfLoss ? next : currentMin);

                Statistics.Add(new Statistics()
                {
                    statisticsHeader = "Most Wins",
                    statisticsValue = winningMatchCount.noOfWins.ToString(),
                    user = Users.FirstOrDefault(u => u.userId == winningMatchCount.userId)

                });

                Statistics.Add(new Statistics()
                {
                    statisticsHeader = "Less Defeats",
                    statisticsValue = lossMatchCount.noOfLoss.ToString(),
                    user = Users.FirstOrDefault(u => u.userId == lossMatchCount.userId)
                });


                OnPropertyChanged(nameof(Statistics));
            }
        }

        public ICommand OpenWebCommand { get; }
    }
}