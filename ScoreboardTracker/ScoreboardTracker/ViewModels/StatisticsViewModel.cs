using System;
using System.Windows.Input;

using Xamarin.Forms;

namespace ScoreboardTracker.ViewModels
{
    public class StatisticsViewModel : BaseViewModel
    {
        public StatisticsViewModel()
        {
            Title = "Statistics";

            OpenWebCommand = new Command(() => Device.OpenUri(new Uri("https://xamarin.com/platform")));
        }

        public ICommand OpenWebCommand { get; }
    }
}