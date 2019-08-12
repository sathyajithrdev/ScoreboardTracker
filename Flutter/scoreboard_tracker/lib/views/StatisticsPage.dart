import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_swiper/flutter_swiper.dart';
import 'package:scoreboard_tracker/models/Game.dart';
import 'package:scoreboard_tracker/models/Statistics.dart';
import 'package:scoreboard_tracker/models/User.dart';
import 'package:scoreboard_tracker/models/UserScore.dart';
import 'package:scoreboard_tracker/repositories/UserScoreRepository.dart';

class StatisticsPage extends StatefulWidget {
  final Color color;

  StatisticsPage(this.color);

  @override
  _StatisticsPageState createState() => _StatisticsPageState();
}

class _StatisticsPageState extends State<StatisticsPage> {
  bool _isLoading = true;

  UserScoreRepository _userScoreRepository = new UserScoreRepository();
  List<User> _users;
  String _groupId;
  List<Statistics> _statistics = new List();
  Key _swipperKey = Key("1");

  @override
  void initState() {
    super.initState();
    populateStatistics();
  }

  Future<void> populateStatistics() async {
    if (_users == null) {
      _users = await _userScoreRepository.getUsers();
    }
    setState(() {
      _isLoading = true;
    });
    _groupId = await _userScoreRepository.getGroupId();
    var completedGames =
        await _userScoreRepository.getAllCompletedGames(_groupId);

    addMaxWinStat(completedGames);
    addLeastDefeatsStat(completedGames);
    addMinScoreStat(completedGames);
    addMostDefeatsStat(completedGames);

    setState(() {
      _statistics = _statistics;
      _isLoading = false;
    });
    _swipperKey = Key(Random.secure().toString());
  }

  void addMaxWinStat(List<Game> completedGames) {
    var winnerGroupData = <String, int>{};
    completedGames.forEach((g) {
      winnerGroupData.putIfAbsent(g.winnerId, () => 0);
      winnerGroupData[g.winnerId] = winnerGroupData[g.winnerId] + 1;
    });

    var maxWinUserId = "";
    int maxWinCount = 0;
    winnerGroupData.forEach((key, value) {
      if (maxWinCount <= value) {
        maxWinUserId = key;
        maxWinCount = value;
      }
    });

    var maxWinUser = _users.firstWhere((u) => u.userId == maxWinUserId);
    if (maxWinUser != null) {
      _statistics.add(new Statistics(
          "Max Wins", maxWinCount.toString(), maxWinUser.profileUrl));
    }
  }

  void addMinScoreStat(List<Game> completedGames) {
    int minScore = 999999;
    String minScoreUserId = "";
    completedGames.forEach((g) {
      int totalScore = 0;
      UserScore userScore =
          g.userScores.firstWhere((u) => u.userId == g.winnerId);
      userScore.scores.forEach((s) => totalScore += s);
      if (totalScore <= minScore) {
        minScore = totalScore;
        minScoreUserId = g.winnerId;
      }
    });

    var minScoreUser = _users.firstWhere((u) => u.userId == minScoreUserId);
    if (minScoreUser != null) {
      _statistics.add(new Statistics(
          "Min Score", minScore.toString(), minScoreUser.profileUrl));
    }
  }

  void addLeastDefeatsStat(List<Game> completedGames) {
    var winnerGroupData = <String, int>{};
    completedGames.forEach((g) {
      winnerGroupData.putIfAbsent(g.looserId, () => 0);
      winnerGroupData[g.looserId] = winnerGroupData[g.looserId] + 1;
    });

    var leastLostUserId = "";
    int leastLostCount = 999999999;
    winnerGroupData.forEach((key, value) {
      if (leastLostCount >= value) {
        leastLostUserId = key;
        leastLostCount = value;
      }
    });

    var leastLostUser = _users.firstWhere((u) => u.userId == leastLostUserId);
    if (leastLostUser != null) {
      _statistics.add(new Statistics("Least Defeats", leastLostCount.toString(),
          leastLostUser.profileUrl));
    }
  }

  void addMostDefeatsStat(List<Game> completedGames) {
    var winnerGroupData = <String, int>{};
    completedGames.forEach((g) {
      winnerGroupData.putIfAbsent(g.looserId, () => 0);
      winnerGroupData[g.looserId] = winnerGroupData[g.looserId] + 1;
    });

    var maxLostUserId = "";
    int maxLostCount = 0;
    winnerGroupData.forEach((key, value) {
      if (maxLostCount <= value) {
        maxLostUserId = key;
        maxLostCount = value;
      }
    });

    var maxLostUser = _users.firstWhere((u) => u.userId == maxLostUserId);
    if (maxLostUser != null) {
      _statistics.add(new Statistics(
          "Most Defeats", maxLostCount.toString(), maxLostUser.profileUrl));
    }
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
      child: Center(
          child: Container(
              height: 400,
              child: new Swiper(
                key: _swipperKey,
                itemBuilder: (BuildContext context, int index) {
                  return new Stack(
                    alignment: Alignment.bottomCenter,
                    children: <Widget>[
                      Image.network(
                        _statistics[index].imageUrl,
                        fit: BoxFit.cover,
                      ),
                      Container(
                          color: Color.fromARGB(160, 10, 10, 10),
                          width: double.infinity,
                          child: Padding(
                            padding: EdgeInsets.all(6),
                            child: Column(
                              children: <Widget>[
                                Text(_statistics[index].value,
                                    style: TextStyle(
                                        color: Colors.amberAccent,
                                        fontSize: 26)),
                                Text(_statistics[index].title,
                                    style: TextStyle(
                                        color: Colors.amberAccent,
                                        fontSize: 18))
                              ],
                              mainAxisSize: MainAxisSize.min,
                            ),
                          ))
                    ],
                  );
                },
                itemCount: _statistics.length,
                viewportFraction: 0.8,
                scale: 0.9,
              ))),
    );
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
}
