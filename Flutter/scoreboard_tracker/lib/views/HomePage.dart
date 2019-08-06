import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';

import 'package:scoreboard_tracker/models/Game.dart';
import 'package:scoreboard_tracker/models/User.dart';
import 'package:scoreboard_tracker/models/UserScore.dart';
import 'package:scoreboard_tracker/repositories/UserScoreRepository.dart';
import 'package:scoreboard_tracker/utils/ObjectUtil.dart';

class HomePage extends StatefulWidget {
  final Color color;

  HomePage(this.color);

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _groupId;
  List<User> _users = new List();
  Game _onGoingGame;

  //Loading indicator properties
  bool _isLoading = true;
  String _loadingMessage = "";

  UserScoreRepository _userRepository;

  _HomePageState() {
    _userRepository = new UserScoreRepository();
  }

  @override
  void initState() {
    super.initState();
    populateUserScores();
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return _loadingView;
    }
    return _homeView;
  }

  void populateUserScores() async {
    showLoading("Loading, please wait");
    await getGroupId();
    await populateUserDetails();
    await populateOnGoingGame();
    await populateUserWinAndLossDetails();
    hideLoading();
  }

  Future populateUserDetails() async {
    var userData = await _userRepository.getUsers();
    setState(() {
      _users = userData;
    });
  }

  Future populateOnGoingGame() async {
    if (_groupId == null) return;

    var onGoingGameData = await _userRepository.getCurrentOnGoingGame(_groupId);

    if (onGoingGameData == null) {
      hideLoading();
      return;
    }

    onGoingGameData.userScores.forEach((s) => {
          s.user = _users.firstWhere((u) {
            return u.userId == s.userId;
          })
        });

    setState(() {
      _onGoingGame = onGoingGameData;
    });
  }

  Future populateUserWinAndLossDetails() async {
    var games = await _userRepository.getAllCompletedGames(_groupId);
    if (games != null && games.isNotEmpty) {
      populateWinCount(games);
      populateLossCount(games);
    }
  }

  void populateLossCount(List<Game> games) {
    var looserGroupData = <String, List<Game>>{};
    games.forEach((g) {
      var list = looserGroupData.putIfAbsent(g.looserId, () => []);
      list.add(g);
    });

    looserGroupData.forEach((looserId, games) {
      UserScore userData =
          _onGoingGame.userScores.firstWhere((u) => u.userId == looserId);

      if (userData != null) {
        setState(() {
          userData.user.lossCount = games.length;
        });
      }
    });
  }

  void populateWinCount(List<Game> games) {
    var winnerGroupData = <String, List<Game>>{};
    games.forEach((g) {
      var list = winnerGroupData.putIfAbsent(g.winnerId, () => []);
      list.add(g);
    });

    winnerGroupData.forEach((winnerId, games) {
      UserScore userData =
          _onGoingGame.userScores.firstWhere((u) => u.userId == winnerId);

      if (userData != null) {
        setState(() {
          userData.user.winCount = games.length;
        });
      }
    });
  }

  Future getGroupId() async {
    _groupId = await _userRepository.getGroupId();
  }

  void showLoading(String message) {
    setState(() {
      _loadingMessage = message;
      _isLoading = true;
    });
  }

  void hideLoading() {
    setState(() {
      _isLoading = false;
    });
  }

  Widget get _loadingView {
    return new Container(
      padding: EdgeInsets.all(0),
      decoration: new BoxDecoration(
        image: new DecorationImage(
          image: new AssetImage("assets/images/background.jpg"),
          fit: BoxFit.fill,
        ),
      ),
      child: new Center(
          child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          new CircularProgressIndicator(),
          Padding(
            padding: EdgeInsets.only(
              top: 32,
            ),
            child: Text(
              _loadingMessage,
              style:
                  TextStyle(fontWeight: FontWeight.bold, color: Colors.white),
            ),
          )
        ],
      )),
    );
  }

  Widget get _homeView {
    return new Container(
        padding: EdgeInsets.all(0),
        decoration: new BoxDecoration(
          image: new DecorationImage(
            image: new AssetImage("assets/images/background.jpg"),
            fit: BoxFit.fill,
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.end,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            Expanded(
                child: ListView.builder(
              itemCount: _onGoingGame?.userScores?.length ?? 0,
              itemBuilder: (context, index) {
                return getUserCard(_onGoingGame.userScores[index]);
              },
            )),
            SizedBox(
              width: double.infinity,
              child: RaisedButton(
                materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                child: Text("Finish"),
                textColor: Colors.white,
                highlightColor: Colors.deepPurple,
                color: Colors.indigoAccent,
                onPressed: onFinishButtonPress,
              ),
            ),
          ],
        ));
  }

  void onFinishButtonPress() {
    showLoading("SavingData");
    if (_validateGameScore()) {
      updateGameResult();
    }
    hideLoading();
  }

  Future<void> updateGameResult() async {
    String winnerUserId;
    String looserUserId;
    int minScore;
    int maxScore;

    _onGoingGame.userScores.forEach((g) {
      var totalScore = g.scores.reduce((a, b) => a + b);

      if (minScore == null || minScore >= totalScore) {
        winnerUserId = g.userId;
        minScore = totalScore;
      }
      if (maxScore == null || maxScore <= totalScore) {
        looserUserId = g.userId;
        maxScore = totalScore;
      }
    });

    _onGoingGame.winnerId = winnerUserId;
    _onGoingGame.looserId = looserUserId;

    if (_onGoingGame.winnerId == null || _onGoingGame.winnerId.trim() == "") {
      _showToast("Unable to calculate winner");
      return;
    }

    if (_onGoingGame.looserId == null || _onGoingGame.looserId.trim() == "") {
      _showToast("Unable to calculate looser");
      return;
    }

    _onGoingGame.isCompleted = true;
    await _userRepository.updateScore(_groupId, _onGoingGame);
    var newGame = new Game.newGame(_users);
    debugPrint("new game json is: " + newGame.toJson().toString());
    await _userRepository.addNewGame(_groupId, newGame);
    await populateOnGoingGame();
    await populateUserWinAndLossDetails();
  }

  Card getUserCard(UserScore user) {
    return Card(
      elevation: 6,
      color: Color.fromARGB(220, 255, 255, 255),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(6.0)),
      ),
      margin: const EdgeInsets.all(8),
      child: Row(
        children: <Widget>[getUserDetailWidget(user), getUserScoreWidget(user)],
      ),
    );
  }

  Expanded getUserScoreWidget(UserScore user) {
    return Expanded(
      child: GridView.count(
        crossAxisCount: 3,
        shrinkWrap: true,
        crossAxisSpacing: 0,
        mainAxisSpacing: 0,
        physics: new NeverScrollableScrollPhysics(),
        padding: EdgeInsets.all(0),
        children: List.generate(8, (index) {
          return (getUserScoreEdit(index, user));
        }),
      ),
    );
  }

  Stack getUserDetailWidget(UserScore user) {
    return new Stack(
      alignment: AlignmentDirectional.bottomStart,
      children: <Widget>[
        Image.network(
          "https://photogallery.indiatimes.com/photo/64290957.cms",
          width: 150,
          height: 250,
          fit: BoxFit.cover,
        ),
        Container(
          color: Color.fromARGB(160, 10, 10, 10),
          width: 150,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              Padding(
                child: Text(
                  user.user.userName,
                  style: TextStyle(
                      color: Colors.red,
                      fontWeight: FontWeight.bold,
                      fontSize: 18),
                ),
                padding: EdgeInsets.all(6),
              ),
              Padding(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: <Widget>[
                    Text(
                      user.user.winCount?.toString() ?? "",
                      style: TextStyle(
                          color: Colors.green,
                          fontWeight: FontWeight.bold,
                          fontSize: 16),
                    ),
                    Text(
                      user.user.lossCount?.toString() ?? "",
                      style: TextStyle(
                          color: Colors.red,
                          fontWeight: FontWeight.bold,
                          fontSize: 16),
                    ),
                  ],
                ),
                padding: EdgeInsets.all(6),
              )
            ],
          ),
        )
      ],
    );
  }

  Padding getUserScoreEdit(int index, UserScore userScore) {
    if (index < 7)
      return Padding(
        padding: EdgeInsets.all(12),
        child: TextField(
          onChanged: (text) {
            updateScore(index, text, userScore);
          },
          keyboardType: TextInputType.number,
          inputFormatters: [WhitelistingTextInputFormatter.digitsOnly],
          maxLines: 1,
          controller: new TextEditingController.fromValue(new TextEditingValue(
              text: (userScore.scores[index] ?? "").toString(),
              selection: new TextSelection.collapsed(
                  offset:
                      userScore.scores[index]?.toString()?.length ?? 1 - 1))),
        ),
      );

    return Padding(
        padding: EdgeInsets.only(left: 12, top: 6, right: 12, bottom: 18),
        child: Center(
          child: Text(
            getTotalScore(userScore),
            style: TextStyle(
                color: Colors.indigo,
                fontSize: 18,
                fontWeight: FontWeight.bold),
          ),
        ));
  }

  void updateScore(int index, String text, UserScore userScore) {
    onUserScoreChanged(userScore, index, text);
  }

  void onUserScoreChanged(UserScore userScore, int index, String text) {
    setState(() {
      userScore.scores[index] =
          ObjectUtil.isNullOrEmpty(text) ? null : int.parse(text);
    });

    updateUserScore();
  }

  void updateUserScore() async {
    await _userRepository.updateScore(_groupId, _onGoingGame);
  }

  String getTotalScore(UserScore userScore) {
    int total = 0;
    userScore.scores.forEach((s) => total += s ?? 0);
    return total.toString();
  }

  bool _validateGameScore() {
    if (_onGoingGame == null ||
        _onGoingGame.userScores == null ||
        _onGoingGame.userScores.length <= 0) {
      return false;
    }

    if (_onGoingGame.userScores.any((us) => us.scores.any((s) => s == null))) {
      _showToast("Please enter scores for all sets");
      return false;
    }
    return true;
  }

  void _showToast(String message) {
    Fluttertoast.showToast(
        msg: message,
        toastLength: Toast.LENGTH_LONG,
        gravity: ToastGravity.BOTTOM,
        timeInSecForIos: 1,
        textColor: Colors.red,
        fontSize: 16.0);
  }
}
