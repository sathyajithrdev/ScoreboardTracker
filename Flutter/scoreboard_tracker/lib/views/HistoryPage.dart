import 'dart:math';

import 'package:flutter/material.dart';
import 'package:scoreboard_tracker/models/Game.dart';
import 'package:scoreboard_tracker/models/User.dart';
import 'package:scoreboard_tracker/repositories/UserScoreRepository.dart';

class HistoryPage extends StatefulWidget {
  final Color color;

  HistoryPage(this.color);

  @override
  _HistoryPageState createState() => _HistoryPageState();
}

class _HistoryPageState extends State<HistoryPage> {
  bool _isLoading = true;

  UserScoreRepository _userScoreRepository = new UserScoreRepository();
  List<User> _users;
  String _groupId;
  List<Game> _games = new List();
  Key _listKey = Key("1");

  @override
  void initState() {
    super.initState();
    populateGames();
  }

  Future<void> populateGames() async {
    if (_users == null) {
      _users = await _userScoreRepository.getUsers();
    }
    setState(() {
      _isLoading = true;
    });
    _groupId = await _userScoreRepository.getGroupId();
    _games =
        await _userScoreRepository.getAllCompletedGamesWithoutDummy(_groupId);
    setState(() {
      _games = _games;
      _isLoading = false;
      _listKey = Key(Random.secure().toString());
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) return _loadingView;

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
                key: _listKey,
                child: ListView.builder(
                  itemCount: _games?.length ?? 0,
                  itemBuilder: (context, index) {
                    return getUserCard(_games[index]);
                  },
                )),
          ],
        ));
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
              "Loading statistics",
              style:
                  TextStyle(fontWeight: FontWeight.bold, color: Colors.white),
            ),
          )
        ],
      )),
    );
  }

  Card getUserCard(Game game) {
    return Card(
      elevation: 6,
      color: Color.fromARGB(220, 255, 255, 255),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(6.0)),
      ),
      margin: const EdgeInsets.all(8),
      child: Row(
        children: <Widget>[getUserDetailWidget(game), getUserScoreWidget(game)],
      ),
    );
  }

  Stack getUserDetailWidget(Game game) {
    var winningUser = _users.firstWhere((u) => u.userId == game.winnerId);
    return new Stack(
      alignment: AlignmentDirectional.bottomStart,
      children: <Widget>[
        Image.network(
          winningUser.profileUrl,
          width: 150,
          height: 250,
          fit: BoxFit.cover,
        )
      ],
    );
  }

  Widget getUserScoreWidget(Game game) {
    List<Padding> texts = new List();
    Map<String, int> totalScores = new Map();
    game.userScores.forEach((us) {
      String userName =
          _users.firstWhere((u) => u.userId == us.userId)?.userName ?? "";
      int totalScore = 0;
      us.scores.forEach((s) => totalScore += s);
      totalScores.putIfAbsent(userName, () => totalScore);
    });

    var sortedData = totalScores.entries.toList();
    sortedData.sort((curr, next) => curr.value - next.value);

    sortedData.forEach((map) {
      texts.add(Padding(
          padding: EdgeInsets.all(16),
          child: Text(
            "${map.key} : ${map.value}",
            textAlign: TextAlign.start,
            style: TextStyle(fontSize: 16),
          )));
    });

    return Column(
      children: texts,
      mainAxisSize: MainAxisSize.min,
    );
  }
}
