using System;
using System.ComponentModel;
using Xamarin.Forms;
using ScoreboardTracker.ViewModels;
using System.Diagnostics;
using System.Threading.Tasks;

namespace ScoreboardTracker.Views
{
    // Learn more about making custom code visible in the Xamarin.Forms previewer
    // by visiting https://aka.ms/xamarinforms-previewer
    [DesignTimeVisible(false)]
    public partial class HomePage : ContentPage
    {
        MainViewModel viewModel;

        public string Test => "From page object";

        public HomePage()
        {
            try
            {
                //viewModel = new MainViewModel();
                InitializeComponent();
                //BindingContext = viewModel;
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
            }

        }

        protected override void OnAppearing()
        {
            base.OnAppearing();
            Task.Run(async () =>
            {
                await Task.Delay(5000);
                viewModel.LoadUsersCommand.Execute(null);
            });
        }
    }
}