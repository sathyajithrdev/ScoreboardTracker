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
using ScoreboardTracker.Droid.Pages;
using ScoreboardTracker.Views;
using Com.Bumptech.Glide;
using Com.Bumptech.Glide.Request;
using System.Linq;
using ScoreboardTracker.Common;
using ScoreboardTracker.Common.Interfaces;
using Plugin.Toast;
using System.Collections.Generic;
using Android.Animation;
using Android.Support.Constraints;
using Android.Support.Design.Widget;
using Android.Support.V4.View;
using Autofac;
using Com.Airbnb.Lottie;
using Java.Lang;
using Xamarin.Essentials;
using static Android.Support.V7.Widget.GridLayoutManager;
using Exception = System.Exception;
using Group = Android.Support.Constraints.Group;
using Math = System.Math;

[assembly: ExportRenderer(typeof(HomePage), typeof(HomePageRenderer))]
namespace ScoreboardTracker.Droid.Pages
{
    public class HomePageRenderer : PageRenderer, IGameScoreHandlerListener, IPage, ValueAnimator.IAnimatorUpdateListener, Animator.IAnimatorListener
    {
        private Android.Widget.Button _buttonStartGame;
        private Android.Widget.Button _buttonFinishGame;
        private Android.Widget.Button _buttonRetry;
        private TextView _textViewLastSetReached;
        private TextView _textViewProgress;
        private TextView _textViewError;
        private View _view;
        private Group _groupControls;
        private Group _groupProgressControls;
        private Group _groupErrorControls;
        private ConstraintLayout _groupWinner;
        private LottieAnimationView _progressView;
        private LottieAnimationView _winnerView;
        private ImageView _ivWinner;
        private int _cxPosition;
        private int _cyPosition;
        private float _finalRadius;
        private bool _isWinnerRevealing;
        private UserScoreAdapter _userScoreAdapter;


        private readonly MainViewModel _viewModel;
        private Activity _activity;
        private RecyclerView _rvUsers;
        private bool _isShowingOfflineSnackbar;
        private bool _neglectFcmChange;

        //private ProgressDialog _mProgressDialog;

        public HomePageRenderer(Context context) : base(context)
        {
            _viewModel = new MainViewModel(this, App.DiResolver.Resolve<IScoreboardRepository>());
            _viewModel.setListener(this);
            initConnectivityListener();
        }

        private void initConnectivityListener()
        {
            Connectivity.ConnectivityChanged += (sender, args) =>
            {
                if (Connectivity.NetworkAccess != NetworkAccess.Internet)
                {
                    _isShowingOfflineSnackbar = true;
                    showSnackbar("You are offline");
                }
                else if (_isShowingOfflineSnackbar)
                {
                    _isShowingOfflineSnackbar = false;
                    showSnackbar("You are back online", Snackbar.LengthShort);
                }

            };
        }

        private void showSnackbar(string message, int duration = Snackbar.LengthIndefinite)
        {
            Snackbar.Make(this, message, duration).Show();
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
                AddView(_view);
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
                await _viewModel.initGroupAndUsers();
                dismissProgressDialog();
            });
        }

        void SetupUserInterface()
        {
            _activity = Context as Activity;
            _view = _activity?.LayoutInflater.Inflate(Resource.Layout.HomePage, this, false);
            _activity?.Window.SetSoftInputMode(SoftInput.AdjustResize);
        }

        private void refreshUserScores()
        {
            //if (!_neglectFcmChange)
            //{
                _userScoreAdapter.setUserDetails(_viewModel.CurrentGame);
            //}
            //_neglectFcmChange = false;
        }

        void initControlsAndEventHandlers()
        {
            initTextViews();
            initGroups();
            initButtonStart();
            initButtonStop();
            initButtonRetry();
            initRecyclerView();
        }

        private void initRecyclerView()
        {
            _rvUsers = _view.FindViewById<RecyclerView>(Resource.Id.rvUsers);
            var layoutManager = new LinearLayoutManager(_activity);
            _rvUsers.SetLayoutManager(layoutManager);
            _userScoreAdapter = new UserScoreAdapter(_viewModel.CurrentGame, this);
            _rvUsers.SetAdapter(_userScoreAdapter);
            ViewCompat.SetNestedScrollingEnabled(_rvUsers, false);
        }

        private void initTextViews()
        {
            _textViewLastSetReached = _view.FindViewById<TextView>(Resource.Id.tvLastGameStat);
            _textViewProgress = _view.FindViewById<TextView>(Resource.Id.textViewInProgress);
            _textViewError = _view.FindViewById<TextView>(Resource.Id.textViewError);
        }

        private void initGroups()
        {
            _groupControls = _view.FindViewById<Group>(Resource.Id.controls);
            _groupProgressControls = _view.FindViewById<Group>(Resource.Id.progressControls);
            _groupErrorControls = _view.FindViewById<Group>(Resource.Id.errorControls);
            _groupWinner = _view.FindViewById<ConstraintLayout>(Resource.Id.clWinner);
            _progressView = _view.FindViewById<LottieAnimationView>(Resource.Id.progressBar);
            _winnerView = _view.FindViewById<LottieAnimationView>(Resource.Id.animViewWinner);
            _ivWinner = _view.FindViewById<ImageView>(Resource.Id.ivWinner);
        }

        private void initButtonStop()
        {
            _buttonFinishGame = _view.FindViewById<Android.Widget.Button>(Resource.Id.buttonEndGame);
            _buttonFinishGame.Click += (sender, e) => { onFinishGameButtonClicked(); };
        }

        private void onFinishGameButtonClicked()
        {
            if (Connectivity.NetworkAccess != NetworkAccess.Internet)
            {
                showError("No internet", onFinishGameButtonClicked);
                return;
            }

            Task.Run(async () =>
            {
                showProgressDialog("Saving data");
                var result = await _viewModel.onEndGame(_viewModel.CurrentGame);
                if (result.Item1)
                {
                    if (!string.IsNullOrWhiteSpace(result.Item2))
                    {
                        ShowToast(result.Item2);
                    }

                    _activity.RunOnUiThread(() =>
                    {
                        var winnerProfile = _viewModel.CurrentGame.scores
                            .FirstOrDefault(u => u.userId == _viewModel.WinnerId)?.user.profileUrl;

                        showWinnerAnimation(winnerProfile);

                        _buttonStartGame.Visibility = ViewStates.Visible;
                        _buttonFinishGame.Visibility = ViewStates.Gone;
                        //_textViewLastSetReached.Visibility = ViewStates.Invisible;
                        //_textViewLastSetReached.Text = string.Empty;
                    });
                    await Task.Delay(6000);
                    _viewModel.CurrentGame = null;
                    await _viewModel.onStartGame();
                    await _viewModel.populatePlayerDetails();
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
        }

        private void showWinnerAnimation(string winnerImageUrl)
        {
            if (_cxPosition == 0)
            {
                _cxPosition = _ivWinner.MeasuredWidth / 2;
                _cyPosition = _ivWinner.MeasuredHeight / 2;
                _finalRadius = Math.Max(_ivWinner.Width, val2: _ivWinner.Height);
            }

            Glide
                .With(_activity)
                .Load(winnerImageUrl)
                .Apply(RequestOptions.CenterCropTransform()).Into(_ivWinner);

            _isWinnerRevealing = true;


            Animator anim =
                ViewAnimationUtils.CreateCircularReveal(_groupWinner, _cxPosition, _cyPosition, 0, _finalRadius);

            _groupWinner.Visibility = ViewStates.Visible;
            _winnerView.PlayAnimation();
            anim.SetDuration(2000);
            anim.AddListener(this);
            anim.Start();
        }

        private void initButtonRetry()
        {
            _buttonRetry = _view.FindViewById<Android.Widget.Button>(Resource.Id.buttonRetry);
        }

        private void initButtonStart()
        {
            _buttonStartGame = _view.FindViewById<Android.Widget.Button>(Resource.Id.buttonStartGame);
            _buttonStartGame.Click += (sender, e) => { onStartGameButtonClicked(); };
        }

        private void onStartGameButtonClicked()
        {
            if (Connectivity.NetworkAccess != NetworkAccess.Internet)
            {
                showError("No internet", onStartGameButtonClicked);
                return;
            }

            Task.Run(async () =>
            {
                var result = await _viewModel.onStartGame();
                if (result.Item1)
                {
                    _activity.RunOnUiThread(() => { setStartAndStopButtonVisibility(result.Item1); });
                }
            });
        }

        private void showError(string message, Action action)
        {
            _groupErrorControls.Visibility = ViewStates.Visible;
            _groupControls.Visibility = ViewStates.Gone;
            _groupProgressControls.Visibility = ViewStates.Gone;
            _textViewError.Text = message;
            _buttonRetry.Click += (sender, args) => { action.Invoke(); };
        }

        private void setStartAndStopButtonVisibility(bool hasInProgressGame)
        {
            _buttonStartGame.Visibility = hasInProgressGame ? ViewStates.Gone : ViewStates.Visible;
            _buttonFinishGame.Visibility = hasInProgressGame ? ViewStates.Visible : ViewStates.Gone;
        }

        protected override void OnLayout(bool changed, int l, int t, int r, int b)
        {
            base.OnLayout(changed, l, t, r, b);

            var msw = MeasureSpec.MakeMeasureSpec(r - l, MeasureSpecMode.Exactly);
            var msh = MeasureSpec.MakeMeasureSpec(b - t, MeasureSpecMode.Exactly);

            _view.Measure(msw, msh);
            _view.Layout(0, 0, r - l, b - t);
        }

        public void OnScoreChanged()
        {
            _neglectFcmChange = true;
            _viewModel.onScoreChangedListener(_viewModel.CurrentGame);
        }

        public void onUserScoresChanged()
        {
            if (_viewModel.CurrentGame == null)
            {
                return;
            }
            _activity.RunOnUiThread(() =>
            {
                setStartAndStopButtonVisibility(!_viewModel.CurrentGame.isCompleted);
                refreshUserScores();
            });
        }


        public void onLastSetReached(UserScore firstUser, UserScore secondUser, string message)
        {
            if (message != null)
            {
                _activity.RunOnUiThread(() =>
                {
                    _textViewLastSetReached.Visibility = ViewStates.Visible;
                    _textViewLastSetReached.Text = message;
                });

            }
            else if (_textViewLastSetReached.Visibility == ViewStates.Visible)
            {
                _activity.RunOnUiThread(() =>
                {
                    _textViewLastSetReached.Visibility = ViewStates.Gone;
                });
            }
        }

        public Task DisplayAlert(string message)
        {
            return Task.Run(() =>
            {
                _activity.RunOnUiThread(() =>
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Context);
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
            _activity.RunOnUiThread(() =>
            {
                CrossToastPopUp.Current.ShowToastMessage(message, Plugin.Toast.Abstractions.ToastLength.Long);
            });
        }

        private void showProgressDialog(string message)
        {
            _activity.RunOnUiThread(() =>
            {
                _textViewProgress.Text = message;
                _groupControls.Visibility = ViewStates.Invisible;
                _groupProgressControls.Visibility = ViewStates.Visible;
                _progressView.PlayAnimation();
            });
        }

        private void dismissProgressDialog()
        {
            _activity.RunOnUiThread(() =>
            {
                //_mProgressDialog?.Dismiss();
                _groupControls.Visibility = ViewStates.Visible;
                _groupProgressControls.Visibility = ViewStates.Gone;
                _progressView.CancelAnimation();
            });
        }

        public void OnAnimationUpdate(ValueAnimator animation)
        {
            float value = ((Float)(animation.AnimatedValue)).FloatValue();
            // Set translation of your view here. Position can be calculated
            // out of value. This code should move the view in a half circle.
            _ivWinner.TranslationX = ((float)(350.0 * Math.Sin(value * Math.PI)));
            _ivWinner.TranslationY = ((float)(20.0 * Math.Cos(value * Math.PI)));
        }

        public void OnAnimationCancel(Animator animation)
        {
        }

        public void OnAnimationEnd(Animator animation)
        {
            if (_isWinnerRevealing)
            {
                Animator animClose =
                    ViewAnimationUtils.CreateCircularReveal(_groupWinner, _cxPosition, _cyPosition, _finalRadius, 0);
                animClose.StartDelay = 3000;
                animClose.SetDuration(2000);
                animClose.AddListener(this);
                _isWinnerRevealing = false;
                animClose.Start();
            }
            else
            {
                _groupWinner.Visibility = ViewStates.Gone;
                _winnerView.CancelAnimation();
            }


        }

        public void OnAnimationRepeat(Animator animation)
        {
        }

        public void OnAnimationStart(Animator animation)
        {

        }
    }



    public class UserScoreAdapter : RecyclerView.Adapter
    {
        private Game _mGame;
        private readonly HomePageRenderer _mContext;
        private ScoreAdapter _scoreAdapter;

        public UserScoreAdapter(Game game, HomePageRenderer context)
        {
            _mGame = game;
            _mContext = context;
            _scoreAdapter = new ScoreAdapter(context, new UserScore());
        }

        public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.From(parent.Context).
                        Inflate(Resource.Layout.item_user, parent, false);

            UserScoreViewHolder vh = new UserScoreViewHolder(itemView);
            return vh;
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            UserScoreViewHolder vh = holder as UserScoreViewHolder;
            if (vh == null) return;
            UserScore userScore = _mGame.scores[position];
            vh.TvUserName.Text = userScore.user.name;
            vh.TvWins.Text = userScore.user.winCount.ToString();
            vh.TvLoss.Text = userScore.user.lossCount.ToString();
            Glide
                .With(_mContext)
                .Load(userScore.user.profileUrl)
                .Apply(RequestOptions.CenterCropTransform()).Into(vh.IvUserProfile);

            GridLayoutManager layoutManager = new GridLayoutManager(_mContext.Context, 3);
            layoutManager.SetSpanSizeLookup(new ScoreSpanSizeLookup(userScore.scores.Count));
            vh.RvUserScore.SetLayoutManager(layoutManager);
            _scoreAdapter = new ScoreAdapter(_mContext, userScore);
            vh.RvUserScore.SetAdapter(_scoreAdapter);
        }

        internal void setUserDetails(Game currentGame)
        {
            _mGame = currentGame;

            NotifyDataSetChanged();
        }

        public override int ItemCount => _mGame?.scores?.Count ?? 0;
    }

    public class ScoreSpanSizeLookup : SpanSizeLookup
    {
        private readonly int _count;

        public ScoreSpanSizeLookup(int count)
        {
            _count = count;
        }

        public override int GetSpanSize(int position)
        {
            return position == _count - 1 ? 3 : 1;
        }
    }


    public class UserScoreViewHolder : RecyclerView.ViewHolder
    {
        public ImageView IvUserProfile { get; }
        public TextView TvUserName { get; }
        public TextView TvWins { get; }
        public TextView TvLoss { get; }
        public RecyclerView RvUserScore { get; }

        public UserScoreViewHolder(View itemView) : base(itemView)
        {
            TvUserName = itemView.FindViewById<TextView>(Resource.Id.tvUserName);
            TvWins = itemView.FindViewById<TextView>(Resource.Id.tvWins);
            TvLoss = itemView.FindViewById<TextView>(Resource.Id.tvLoss);
            IvUserProfile = itemView.FindViewById<ImageView>(Resource.Id.ivUser);
            RvUserScore = itemView.FindViewById<RecyclerView>(Resource.Id.rvScore);
        }
    }


    public class ScoreAdapter : RecyclerView.Adapter
    {
        public static int EditTextOnlyType = 0;
        public static int EditTextAndTextViewType = 1;

        private const int TotalValueChanged = 1;

        private readonly UserScore _mUserScore;
        private readonly HomePageRenderer _mContext;
        private int _isInitialLoad;

        public ScoreAdapter(HomePageRenderer context, UserScore userScore)
        {
            _mUserScore = userScore;
            _mContext = context;
        }

        public override int GetItemViewType(int position)
        {
            return _mUserScore.scores.Count == position + 1 ? EditTextAndTextViewType : EditTextOnlyType;
        }


        public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup viewGroup, int type)
        {
            var itemView = LayoutInflater.From(viewGroup.Context).Inflate(type == EditTextAndTextViewType ?
                Resource.Layout.item_score_with_text : Resource.Layout.item_score, viewGroup, false);
            return new ScoreViewHolder(itemView, type);
        }

        public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            if (_isInitialLoad < _mUserScore.scores.Count)
            {
                _isInitialLoad++;
            }
            ScoreViewHolder vh = holder as ScoreViewHolder;
            if (vh == null) return;

            vh.EtScore.Text = _mUserScore.scores[position]?.ToString();
            SetTotalScoreTextBox(vh);
            vh.EtScore.AfterTextChanged += (sender, args) =>
            {

                if (_mUserScore.scores[position] == CommonUtils.NullIfEmpty(vh.EtScore.Text))
                    return;

                _mUserScore.scores[position] = CommonUtils.NullIfEmpty(vh.EtScore.Text);
                

                if (position == _mUserScore.scores.Count - 1)
                {
                    vh.TvTotalScore.Text = _mUserScore.scores.Sum(s => s).ToString();
                }
                else
                {
                    if (_isInitialLoad == _mUserScore.scores.Count)
                    {
                        NotifyItemChanged(_mUserScore.scores.Count - 1, TotalValueChanged);
                        _mContext.OnScoreChanged();
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
                vh.TvTotalScore.Text = _mUserScore.scores.Sum(s => s).ToString();
            }
        }

        public override int ItemCount => _mUserScore?.scores?.Count ?? 0;

    }


    public class ScoreViewHolder : RecyclerView.ViewHolder
    {
        public EditText EtScore { get; }
        public TextView TvTotalScore { get; }

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
