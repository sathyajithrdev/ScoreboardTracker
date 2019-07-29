using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using Xamarin.Forms;

using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Models;
using Xamarin.Essentials;

namespace ScoreboardTracker.ViewModels
{
    public class StatisticsViewModel : BaseViewModel
    {
        private ObservableCollection<Statistics> _statistics;
        private readonly IScoreboardRepository _scoreboardRepository;
        public ObservableCollection<Statistics> Statistics
        {
            get => _statistics;
            set => SetProperty(ref _statistics, value);
        }
        public StatisticsViewModel(IScoreboardRepository scoreboardRepository)
        {
            _scoreboardRepository = scoreboardRepository;
            Title = "Statistics";
            populateStatisticsData();
            MessagingCenter.Subscribe<MainViewModel>(this, "gameCompleted", (sender) =>
            {
                MainThread.BeginInvokeOnMainThread(populateStatisticsData);
            });
        }

        private async void populateStatisticsData()
        {
            var group = await _scoreboardRepository.GetGroup();

            if (group == null)
            {
                return;
            }

            List<User> allUsers = await _scoreboardRepository.GetAllUsers();

            var users = allUsers.Where(u => group.userIds.Contains(u.userId)).ToList();

            Statistics = new ObservableCollection<Statistics>();

            var games = await _scoreboardRepository.GetGames(group.groupId);

            if (games == null)
            {
                return;
            }

            var completedGames = games.Where(g => g.isCompleted).ToList();

            var winningMatchCount = completedGames.GroupBy(g => g.winnerId)
                .Select(w => new { userId = w.Key, noOfWins = w.Count() })
                .Aggregate((currentMax, next) => currentMax == null || next.noOfWins > currentMax.noOfWins ? next : currentMax);

            var enumerable = completedGames.GroupBy(g => g.looserId).Select(w => new { userId = w.Key, noOfLoss = w.Count() }).ToList();
            var lossMatchCount = enumerable
                .Aggregate((currentMin, next) => currentMin == null || next.noOfLoss < currentMin.noOfLoss ? next : currentMin);

            var maxLossMatchCount = enumerable
                .Aggregate((currentMax, next) => currentMax == null || next.noOfLoss > currentMax.noOfLoss ? next : currentMax);

            var minScore = completedGames
                .Aggregate((currentMin, next) => currentMin == null || next.scores.Min(s => s.scores.Sum()) < currentMin.scores.Min(s => s.scores.Sum()) ? next : currentMin);

            var maxScore = completedGames
                .Aggregate((currentMax, next) => currentMax == null || next.scores.Max(s => s.scores.Sum()) > currentMax.scores.Max(s => s.scores.Sum()) ? next : currentMax);


            Statistics.Add(new Statistics()
            {
                statisticsHeader = "Most Wins",
                statisticsValue = winningMatchCount.noOfWins.ToString(),
                user = users.FirstOrDefault(u => u.userId == winningMatchCount.userId)

            });

            Statistics.Add(new Statistics()
            {
                statisticsHeader = "Least Defeats",
                statisticsValue = lossMatchCount.noOfLoss.ToString(),
                user = users.FirstOrDefault(u => u.userId == lossMatchCount.userId)
            });

            Statistics.Add(new Statistics()
            {
                statisticsHeader = "Minimum Score",
                statisticsValue = minScore.scores.FirstOrDefault(s => s.userId == minScore.winnerId)?.scores.Sum().ToString(),
                user = users.FirstOrDefault(u => u.userId == minScore.winnerId)
            });

            //Statistics.Add(new Statistics()
            //{
            //    statisticsHeader = "Max Score Defeat",
            //    statisticsValue = maxScore.scores.FirstOrDefault(s => s.userId == maxScore.looserId)?.scores.Sum().ToString(),
            //    user = users.FirstOrDefault(u => u.userId == maxScore.looserId)
            //});

            Statistics.Add(new Statistics()
            {
                statisticsHeader = "Most Defeats",
                statisticsValue = maxLossMatchCount.noOfLoss.ToString(),
                user = users.FirstOrDefault(u => u.userId == maxLossMatchCount.userId)
            });

            OnPropertyChanged(nameof(Statistics));
        }
    }
}