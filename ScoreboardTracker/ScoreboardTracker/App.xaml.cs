using System;
using Xamarin.Forms;
using System.Diagnostics;
using Autofac;

using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Services;
using ScoreboardTracker.Views;

namespace ScoreboardTracker
{
    public partial class App : Application
    {

        public static IContainer DiResolver;

        public App()
        {
            try
            {
                InitializeComponent();
                InitIocComponents();
                MainPage = new MainPage();
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
            }
        }

        private void InitIocComponents()
        {
            var builder = new ContainerBuilder();

            // Register individual components
            builder.RegisterInstance(new ScoreboardRepository()).As<IScoreboardRepository>();

            DiResolver = builder.Build();
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
