using Plugin.Toast;
using System.Threading.Tasks;
using Xamarin.Forms;

namespace ScoreboardTracker.Common.Interfaces
{
    public class BasePage : ContentPage, IPage
    {

        public Task DisplayAlert(string message) {
            return DisplayAlert("Scoreboard Tracker", message, "OK");
        }

        public void ShowToast(string message)
        {
            CrossToastPopUp.Current.ShowToastMessage(message);
        }
    }
}
