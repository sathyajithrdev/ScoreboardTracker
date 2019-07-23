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
using System.Linq;
using ScoreboardTracker.Common;
using ScoreboardTracker.Common.Interfaces;
using Plugin.Toast;
using System.Collections.Generic;
using static Android.Support.V7.Widget.GridLayoutManager;

[assembly: ExportRenderer(typeof(HomePage), typeof(HomePageRenderer))]
namespace ScoreboardTracker.Droid.Pages
{
    public partial class HomePageRenderer : PageRenderer, IGameScoreHandlerListener, IPage
    {
        Android.Widget.Button buttonStartGame;
        Android.Widget.Button buttonEndGame;
        TextView textViewLastSetReached;
        View view;
        MainViewModel viewModel;
        Activity activity;
        RecyclerView rvUsers;
        public Context mContext;
        ProgressDialog mProgressDiag;

        public HomePageRenderer(Context context) : base(context)
        {
            viewModel = new MainViewModel(this);
            mContext = context;
            viewModel.setListener(this);
        }

        protected override void OnAttachedToWindow()
        {
            populateUserData();
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
                initControlsAndEventHandlers();
                AddView(view);
            }
            catch (Exception ex)
            {
                ShowToast(ex.Message);
                System.Diagnostics.Debug.WriteLine(@"			ERROR: ", ex.Message);
            }
        }

        private void populateUserData()
        {
            Task.Run(async () =>
            {
                showProgressDialog("Loading details");
                await viewModel.initGroupAndUsers();
                dismissProgressDialog();
            });
        }

        void SetupUserInterface()
        {
            activity = this.Context as Activity;
            view = activity.LayoutInflater.Inflate(Resource.Layout.HomePage, this, false);
        }

        private void initRecyclerView()
        {
            rvUsers = view.FindViewById<RecyclerView>(Resource.Id.rvUsers);
            var layoutManager = new LinearLayoutManager(activity);
            rvUsers.SetLayoutManager(layoutManager);
            UserScoreAdapter mAdapter = new UserScoreAdapter(viewModel.currentGame, this);
            rvUsers.SetAdapter(mAdapter);
        }

        void initControlsAndEventHandlers()
        {
            textViewLastSetReached = view.FindViewById<global::Android.Widget.TextView>(Resource.Id.tvLastGameStat);
            initButtonStart();
            initButtonStop();
        }

        private void initButtonStop()
        {
            buttonEndGame = view.FindViewById<global::Android.Widget.Button>(Resource.Id.buttonEndGame);
            buttonEndGame.Click += (sender, e) =>
            {
                Task.Run(async () =>
                {
                    var result = await viewModel.onEndGame(viewModel.currentGame);

                    showProgressDialog("Saving data");

                    if (result.Item1)
                    {
                        if (!string.IsNullOrWhiteSpace(result.Item2))
                        {
                            ShowToast(result.Item2);
                        }
                        activity.RunOnUiThread(() =>
                        {
                            buttonStartGame.Visibility = ViewStates.Visible;
                            buttonEndGame.Visibility = ViewStates.Gone;
                        });
                        await viewModel.onStartGame();
                    }
                    else
                    {
                        if (!string.IsNullOrWhiteSpace(result.Item2))
                        {
                            ShowToast(result.Item2);
                        }
                    }
                    dismissProgressDialog();
                });
            };
        }

        private void initButtonStart()
        {
            buttonStartGame = view.FindViewById<global::Android.Widget.Button>(Resource.Id.buttonStartGame);
            buttonStartGame.Click += (sender, e) =>
            {
                Task.Run(async () =>
                {
                    var result = await viewModel.onStartGame();
                    if (result.Item1)
                    {
                        activity.RunOnUiThread(() =>
                        {
                            setStartAndStopButtonVisibility(result.Item1);
                        });
                    }
                });
            };
        }

        private void setStartAndStopButtonVisibility(bool hasInProgressGame)
        {
            buttonStartGame.Visibility = hasInProgressGame ? ViewStates.Gone : ViewStates.Visible;
            buttonEndGame.Visibility = hasInProgressGame ? ViewStates.Visible : ViewStates.Gone;
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
            viewModel.onScoreChangedListener(viewModel.currentGame);
        }

        public void onUserScoresChanged()
        {
            if (viewModel.currentGame == null)
            {
                return;
            }
            activity.RunOnUiThread(() =>
            {
                setStartAndStopButtonVisibility(!viewModel.currentGame.isCompleted);
                initRecyclerView();
            });
        }


        public void onLastSetReached(UserScore firstUser, UserScore secondUser, string message)
        {
            if (message != null)
            {
                activity.RunOnUiThread(() =>
                {
                    textViewLastSetReached.Visibility = ViewStates.Visible;
                    textViewLastSetReached.Text = message;
                });

            }
            else if (textViewLastSetReached.Visibility == ViewStates.Visible)
            {
                activity.RunOnUiThread(() =>
                {
                    textViewLastSetReached.Visibility = ViewStates.Gone;
                });
            }
        }

        public Task DisplayAlert(string message)
        {
            return Task.Run(() =>
            {
                activity.RunOnUiThread(() =>
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    AlertDialog alert = dialog.Create();
                    alert.SetTitle("Scoreboard Tracker");
                    alert.SetMessage(message);
                    alert.SetButton("OK", (c, ev) =>
                    {
                        alert.Dispose();
                    });
                    alert.Show();
                });
            });
        }

        public void ShowToast(string message)
        {
            activity.RunOnUiThread(() =>
            {
                CrossToastPopUp.Current.ShowToastMessage(message, Plugin.Toast.Abstractions.ToastLength.Long);
            });
        }

        private void showProgressDialog(string message)
        {
            activity.RunOnUiThread(() =>
            {
                if (mProgressDiag != null)
                {
                    mProgressDiag = new ProgressDialog(activity);
                    mProgressDiag.SetMessage(message);
                    mProgressDiag.Show();
                }
            });
        }

        private void dismissProgressDialog()
        {
            activity.RunOnUiThread(() =>
            {
                mProgressDiag?.Dismiss();
            });
        }
    }



    public class UserScoreAdapter : RecyclerView.Adapter
    {
        private event EventHandler<int> ItemClick;
        private Game mGame;
        private HomePageRenderer mContext;

        public UserScoreAdapter(Game game, HomePageRenderer context)
        {
            mGame = game;
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
            UserScore userScore = mGame.scores[position];
            vh.TvUserName.Text = userScore.user.name;
            Glide
            .With(mContext)
            .Load(userScore.user.profileUrl)
            .Apply(RequestOptions.CenterCropTransform()).Into(vh.IvUserProfile);

            GridLayoutManager layoutManager = new GridLayoutManager(mContext.Context, 3);
            layoutManager.SetSpanSizeLookup(new ScoreSpanSizeLookup(userScore.scores.Count));
            vh.RvUserScore.SetLayoutManager(layoutManager);
            vh.RvUserScore.SetAdapter(new ScoreAdapter(mContext, userScore));
        }

        public override int ItemCount
        {
            get { return mGame.scores.Count; }
        }
        void OnClick(int position)
        {
            ItemClick?.Invoke(this, position);
        }
    }

    public class ScoreSpanSizeLookup : SpanSizeLookup
    {
        private int count;

        public ScoreSpanSizeLookup(int count)
        {
            this.count = count;
        }

        public override int GetSpanSize(int position)
        {
            if (position == count - 1)
                return 3;
            return 1;
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
        public static int EditTextOnlyType = 0;
        public static int EditTextAndTextViewType = 1;

        private const int TotalValueChanged = 1;

        private event EventHandler<int> ItemClick;
        private UserScore mUserScore;
        private readonly HomePageRenderer mContext;
        private int isInitialLoad;

        public ScoreAdapter(HomePageRenderer context, UserScore userScore)
        {
            mUserScore = userScore;
            mContext = context;
        }

        public override int GetItemViewType(int position)
        {
            return mUserScore.scores.Count == position + 1 ? EditTextAndTextViewType : EditTextOnlyType;
        }


        public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup viewGroup, int type)
        {
            View itemView;
            if (type == EditTextAndTextViewType)
            {
                itemView = LayoutInflater.From(viewGroup.Context).
                      Inflate(Resource.Layout.item_score_with_text, viewGroup, false);
            }
            else
            {
                itemView = LayoutInflater.From(viewGroup.Context).
                     Inflate(Resource.Layout.item_score, viewGroup, false);

            }
            return new ScoreViewHolder(itemView, type);
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            if (isInitialLoad < mUserScore.scores.Count)
            {
                isInitialLoad++;
            }
            ScoreViewHolder vh = holder as ScoreViewHolder;
            vh.EtScore.Text = mUserScore.scores[position]?.ToString();
            SetTotalScoreTextBox(vh);
            vh.EtScore.AfterTextChanged += (sender, args) =>
            {
                mUserScore.scores[position] = CommonUtils.NullIfEmpty(vh.EtScore.Text);

                if (position == mUserScore.scores.Count - 1)
                {
                    vh.TvTotalScore.Text = mUserScore.scores.Sum(s => s).ToString();
                }
                else
                {
                    if (isInitialLoad == mUserScore.scores.Count)
                    {
                        NotifyItemChanged(mUserScore.scores.Count - 1, TotalValueChanged);
                        mContext.OnScoreChanged();
                    }
                }
            };
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position, IList<Java.Lang.Object> payloads)
        {
            if (payloads.Any())
            {
                if ((int)payloads[0] == TotalValueChanged)
                {
                    SetTotalScoreTextBox(holder as ScoreViewHolder);
                }
            }
            base.OnBindViewHolder(holder, position, payloads);
        }

        private void SetTotalScoreTextBox(ScoreViewHolder vh)
        {
            if (vh.TvTotalScore != null)
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

        public ScoreViewHolder(View itemView, int type) : base(itemView)
        {
            EtScore = itemView.FindViewById<EditText>(Resource.Id.etScore);
            if (type == ScoreAdapter.EditTextAndTextViewType)
            {
                TvTotalScore = itemView.FindViewById<TextView>(Resource.Id.tvTotalScore);
            }
        }
    }

}