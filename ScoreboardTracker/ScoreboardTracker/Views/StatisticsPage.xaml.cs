using System.ComponentModel;
using Autofac;
using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.ViewModels;
using Xamarin.Forms;

namespace ScoreboardTracker.Views
{
    // Learn more about making custom code visible in the Xamarin.Forms previewer
    // by visiting https://aka.ms/xamarinforms-previewer
    [DesignTimeVisible(false)]
    public partial class StatisticsPage : ContentPage
    {
        public StatisticsPage()
        {
            InitializeComponent();
            BindingContext = new StatisticsViewModel(App.DiResolver.Resolve<IScoreboardRepository>());
        }
    }
}