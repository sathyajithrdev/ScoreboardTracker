using Android.App;
using Android.Content.PM;
using Android.OS;

namespace ScoreboardTracker.Droid
{
    [Activity(Label = "ScoreboardTracker", Icon = "@mipmap/icon", Theme = "@style/Theme.Splash", MainLauncher = true, ConfigurationChanges = ConfigChanges.ScreenSize | ConfigChanges.Orientation)]
    public class SplashScreenActivity : Activity
    {
        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            System.Threading.Thread.Sleep(3000);
            StartActivity(typeof(MainActivity));
            Finish();
        }
    }
}