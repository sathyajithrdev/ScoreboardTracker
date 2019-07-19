using System;
using Xamarin.Forms;
using Xamarin.Forms.Xaml;
using ScoreboardTracker.Services;
using ScoreboardTracker.Views;
using System.Diagnostics;

namespace ScoreboardTracker
{
    public partial class App : Application
    {

        public App()
        {
            try { 
            InitializeComponent();

            MainPage = new MainPage();
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
            }
        }

        protected override void OnStart()
        {
            // Handle when your app starts
        }

        protected override void OnSleep()
        {
            // Handle when your app sleeps
        }

        protected override void OnResume()
        {
            // Handle when your app resumes
        }
    }
}
