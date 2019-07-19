using System;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;
using Android.App;
using Android.Content;
using Android.Views;
using Android.Widget;
using ScoreboardTracker.ViewModels;
using System.Threading.Tasks;
using Android.Support.V7.Widget;
using ScoreboardTracker.Models;
using View = Android.Views.View;
using System.Collections.ObjectModel;
using ScoreboardTracker.Droid.Pages;
using ScoreboardTracker.Views;
using Com.Bumptech.Glide;
using Com.Bumptech.Glide.Request;
using System.Collections.Generic;
using Android.Text;
using System.Linq;
using ScoreboardTracker.Common;

[assembly: ExportRenderer(typeof(HomePage), typeof(HomePageRenderer))]
namespace ScoreboardTracker.Droid.Pages
{
    public partial class HomePageRenderer : PageRenderer
    {
        global::Android.Widget.Button buttonStartGame;
        View view;
        MainViewModel viewModel;
        Activity activity;
        RecyclerView rvUsers;
        public Context mContext;

        public HomePageRenderer(Context context) : base(context)
        {
            viewModel = new MainViewModel();
            mContext = context;
            Task.Run(() => viewModel.LoadUsersCommand.Execute(null));
            viewModel.setUserListChangedListener(() =>
            {
                initRecyclerView();
            });
        }

        protected override void OnElementChanged(ElementChangedEventArgs<Page> e)
        {
            base.OnElementChanged(e);

            if (e.OldElement != null || Element == null)
            {
                return;
            }

            try
            {
                SetupUserInterface();
                SetupEventHandlers();
                AddView(view);
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine(@"			ERROR: ", ex.Message);
            }
        }

        void SetupUserInterface()
        {
            activity = this.Context as Activity;
            view = activity.LayoutInflater.Inflate(Resource.Layout.HomePage, this, false);
        }

        private void initRecyclerView()
        {
            activity.RunOnUiThread(() =>
            {
                rvUsers = view.FindViewById<RecyclerView>(Resource.Id.rvUsers);
                var layoutManager = new LinearLayoutManager(activity);
                rvUsers.SetLayoutManager(layoutManager);
                UserScoreAdapter mAdapter = new UserScoreAdapter(viewModel.Users, this);
                rvUsers.SetAdapter(mAdapter);
            });

        }

        void SetupEventHandlers()
        {
            buttonStartGame = view.FindViewById<global::Android.Widget.Button>(Resource.Id.buttonStartGame);
            buttonStartGame.Click += (sender, e) =>
            {
                viewModel.onStartGame();
            };
            //takePhotoButton.Click += TakePhotoButtonTapped;

            //switchCameraButton = view.FindViewById<global::Android.Widget.Button>(Resource.Id.switchCameraButton);
            //switchCameraButton.Click += SwitchCameraButtonTapped;

            //toggleFlashButton = view.FindViewById<global::Android.Widget.Button>(Resource.Id.toggleFlashButton);
            //toggleFlashButton.Click += ToggleFlashButtonTapped;
        }

        protected override void OnLayout(bool changed, int l, int t, int r, int b)
        {
            base.OnLayout(changed, l, t, r, b);

            var msw = MeasureSpec.MakeMeasureSpec(r - l, MeasureSpecMode.Exactly);
            var msh = MeasureSpec.MakeMeasureSpec(b - t, MeasureSpecMode.Exactly);

            view.Measure(msw, msh);
            view.Layout(0, 0, r - l, b - t);
        }

        public void OnScoreChanged()
        {
            viewModel.onScoreChangedListener(viewModel.Users.Select(u => u.userScore).ToList());
        }
    }



    public class UserScoreAdapter : RecyclerView.Adapter
    {
        private event EventHandler<int> ItemClick;
        private ObservableCollection<User> mUsers;
        private HomePageRenderer mContext;

        public UserScoreAdapter(ObservableCollection<User> users, HomePageRenderer context)
        {
            mUsers = users;
            mContext = context;
        }

        public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.From(parent.Context).
                        Inflate(Resource.Layout.item_user, parent, false);

            UserScoreViewHolder vh = new UserScoreViewHolder(itemView, OnClick);
            return vh;
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            UserScoreViewHolder vh = holder as UserScoreViewHolder;
            vh.TvUserName.Text = mUsers[position].name;
            Glide
            .With(mContext)
            .Load("https://cdn1.thr.com/sites/default/files/imagecache/portrait_300x450/2015/06/johnny_depp_dior.jpg")
            .Apply(RequestOptions.CenterCropTransform()).Into(vh.IvUserProfile);

            vh.RvUserScore.SetLayoutManager(new GridLayoutManager(mContext.Context, 3));
            vh.RvUserScore.SetAdapter(new ScoreAdapter(mContext, mUsers[position].userScore));
        }

        public override int ItemCount
        {
            get { return mUsers.Count; }
        }
        void OnClick(int position)
        {
            ItemClick?.Invoke(this, position);
        }
    }


    public class UserScoreViewHolder : RecyclerView.ViewHolder
    {
        public ImageView IvUserProfile { get; private set; }
        public TextView TvUserName { get; private set; }
        public RecyclerView RvUserScore { get; private set; }

        public UserScoreViewHolder(View itemView, Action<int> listener) : base(itemView)
        {
            TvUserName = itemView.FindViewById<TextView>(Resource.Id.tvUserName);
            IvUserProfile = itemView.FindViewById<ImageView>(Resource.Id.ivUser);
            RvUserScore = itemView.FindViewById<RecyclerView>(Resource.Id.rvScore);
            itemView.Click += (sender, e) => listener(base.LayoutPosition);
        }
    }


    public class ScoreAdapter : RecyclerView.Adapter
    {
        private event EventHandler<int> ItemClick;
        private UserScore mUserScore;
        private readonly HomePageRenderer mContext;
        private int isInitialLoad;

        public ScoreAdapter(HomePageRenderer context, UserScore userScore)
        {
            mUserScore = userScore;
            mContext = context;
        }

        public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.From(parent.Context).
                        Inflate(Resource.Layout.item_score, parent, false);

            ScoreViewHolder vh = new ScoreViewHolder(itemView, OnClick);
            return vh;
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            if (isInitialLoad < mUserScore.scores.Count)
            {
                isInitialLoad++;
            }
            ScoreViewHolder vh = holder as ScoreViewHolder;
            vh.EtScore.Text = mUserScore.scores[position]?.ToString();
            setTotalScoreTextBox(position, vh);
            vh.EtScore.AfterTextChanged += (sender, args) =>
            {
                mUserScore.scores[position] = CommonUtils.ZeroIfEmpty(vh.EtScore.Text);

                if (position == mUserScore.scores.Count - 1)
                {
                    vh.TvTotalScore.Text = mUserScore.scores.Sum(s => s).ToString();
                }
                else
                {
                    if (isInitialLoad == mUserScore.scores.Count)
                    {
                        NotifyItemChanged(mUserScore.scores.Count - 1);
                        mContext.OnScoreChanged();
                    }
                }
            };
        }

        private void setTotalScoreTextBox(int position, ScoreViewHolder vh)
        {
            bool isLastPosition = position == mUserScore.scores.Count - 1;
            vh.TvTotalScore.Visibility = isLastPosition ? ViewStates.Visible : ViewStates.Gone;
            if (position == mUserScore.scores.Count - 1)
            {
                vh.TvTotalScore.Text = mUserScore.scores.Sum(s => s).ToString();
            }
        }

        public override int ItemCount
        {
            get { return mUserScore?.scores?.Count ?? 0; }
        }

        void OnClick(int position)
        {
            ItemClick?.Invoke(this, position);
        }
    }


    public class ScoreViewHolder : RecyclerView.ViewHolder
    {
        public EditText EtScore { get; private set; }
        public TextView TvTotalScore { get; private set; }

        public ScoreViewHolder(View itemView, Action<int> listener) : base(itemView)
        {
            EtScore = itemView.FindViewById<EditText>(Resource.Id.etScore);
            TvTotalScore = itemView.FindViewById<TextView>(Resource.Id.tvTotalScore);
            itemView.Click += (sender, e) => listener(base.LayoutPosition);
        }
    }

}