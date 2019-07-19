using System;

using Android.App;
using Android.OS;
using Android.Runtime;
using Com.Bumptech.Glide;

namespace ScoreboardTracker.Droid
{
    [Application]
    public class MainApplication : Application
    {
        public MainApplication(IntPtr handle, JniHandleOwnership transer) : base(handle, transer)
        {
        }

        public override void OnCreate()
        {
            base.OnCreate();
            //try
            //{
            //    Plugin.CloudFirestore.CloudFirestore.Init(this);
            //    //Firebase.FirebaseApp.GetInstance(DefaultAppName);
            //}
            //catch (Exception ex)
            //{
            //    System.Diagnostics.Debug.WriteLine(ex.Message);
            //}
            //    var baseOptions = Firebase.FirebaseOptions.FromResource(this);
            //    var options = new Firebase.FirebaseOptions.Builder(baseOptions).SetProjectId(baseOptions.StorageBucket.Split('.')[0]).Build();


            //    Firebase.FirebaseApp.InitializeApp(this, options, DefaultAppName);
            //}
        }
    }
}