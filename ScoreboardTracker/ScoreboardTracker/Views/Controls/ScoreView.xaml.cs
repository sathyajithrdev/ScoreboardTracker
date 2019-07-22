using ScoreboardTracker.Models;
using System;
using System.Diagnostics;
using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace ScoreboardTracker.Views.Controls
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class ScoreView : ContentView
    {

        public static readonly BindableProperty UserScoresProperty =
          BindableProperty.Create(nameof(UserScores), typeof(UserScore), typeof(ScoreView), propertyChanged: UserScoresUpdated);


        public UserScore UserScores
        {
            get { return (UserScore)GetValue(UserScoresProperty); }
            set { SetValue(UserScoresProperty, value); }
        }



        //public static readonly BindableProperty UserScoresProperty = BindableProperty.Create(nameof(UserScores), typeof(UserScore), typeof(ScoreView), new UserScore(), BindingMode.OneWay);
        //public UserScore UserScores
        //{
        //    get => (UserScore)GetValue(UserScoresProperty);

        //    set => SetValue(UserScoresProperty, value);
        //}
        public ScoreView()
        {
            UserScores = new UserScore();
            try
            {
                InitializeComponent();
                BindingContext = this;
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
            }
        }

        protected override void OnPropertyChanged(string propertyName = null)
        {
            base.OnPropertyChanged(propertyName);

            if (propertyName == UserScoresProperty.PropertyName)
            {
                addScoreEntryComponent(this);
            }
        }

        private static void addScoreEntryComponent(ScoreView scoreView)
        {
            if (scoreView.stackLayoutScore != null && scoreView.UserScores != null && scoreView.UserScores.scores != null)
            {
                scoreView.stackLayoutScore.Children.Clear();
                scoreView.UserScores.scores.ForEach(s =>
                {
                    Entry item = new Entry() { HorizontalOptions = LayoutOptions.Start, VerticalOptions = LayoutOptions.Start };
                    //item.SetBinding(Entry.TextProperty, new Binding("score"));
                    scoreView.stackLayoutScore.Children.Add(item);
                });
            }
        }

        private static void UserScoresUpdated(object sender, object oldValue, object newValue)
        {
            if (sender is ScoreView scoreView && newValue is UserScore newScore)
            {
                scoreView.UserScores = newScore;
                addScoreEntryComponent(scoreView);
            }
        }
    }
}