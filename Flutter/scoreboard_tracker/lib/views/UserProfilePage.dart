import 'package:flutter/material.dart';
import 'package:charts_flutter/flutter.dart' as charts;
import 'package:scoreboard_tracker/models/Game.dart';
import 'package:scoreboard_tracker/models/UserScore.dart';
import 'package:scoreboard_tracker/repositories/UserScoreRepository.dart';

class UserProfilePage extends StatefulWidget {
  final UserScore _user;

  UserProfilePage(this._user);

  @override
  _UserProfilePageState createState() => _UserProfilePageState(_user);
}

class _UserProfilePageState extends State<UserProfilePage> {
  final UserScore _user;
  final UserScoreRepository _userScoreRepository = new UserScoreRepository();
  List<charts.Series<GameStat, int>> seriesList = [];

  _UserProfilePageState(this._user);

  @override
  void initState() {
    super.initState();
    populatePlayerStatistics();
  }

  @override
  Widget build(BuildContext context) {
    return new Container(
      padding: EdgeInsets.all(0),
      decoration: new BoxDecoration(
        image: new DecorationImage(
          image: new AssetImage("assets/images/background.jpg"),
          fit: BoxFit.fill,
        ),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[getUserProfileUI(), getUserStatGraphs()],
      ),
    );
  }

  Container getUserStatGraphs() {
    return Container(
        height: 300,
        width: 300,
        child: new charts.PieChart(seriesList,
            animate: true,
            defaultRenderer:
                new charts.ArcRendererConfig(arcRendererDecorators: [
              new charts.ArcLabelDecorator(
                  labelPosition: charts.ArcLabelPosition.inside,
                  insideLabelStyleSpec: charts.TextStyleSpec(
                      color: charts.Color.white, fontSize: 16))
            ])));
  }

  Stack getUserProfileUI() {
    return Stack(
      children: <Widget>[
        Stack(children: <Widget>[
          Hero(
              tag: 'profileImage${_user.userId}',
              child: Image.network(
                _user.user.profileUrl,
                height: 300,
                fit: BoxFit.cover,
                width: double.infinity,
              )),
          Padding(
              padding: EdgeInsets.only(left: 1, top: 32),
              child: RaisedButton.icon(
                  onPressed: () => Navigator.of(context).pop(),
                  color: Colors.transparent,
                  elevation: 0,
                  icon: new Icon(
                    Icons.arrow_back,
                    color: Colors.white,
                  ),
                  label: Text("")))
        ]),
        Container(
          color: Color.fromARGB(160, 10, 10, 10),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              Padding(
                child: Hero(
                    tag: 'userName${_user.userId}',
                    child: Text(
                      _user.user.userName,
                      style: TextStyle(
                          decoration: TextDecoration.none,
                          color: Colors.red,
                          fontWeight: FontWeight.bold,
                          fontSize: 18),
                    )),
                padding: EdgeInsets.all(6),
              ),
              Padding(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: <Widget>[
                    Text(
                      _user.user.winCount?.toString() ?? "",
                      style: TextStyle(
                          decoration: TextDecoration.none,
                          color: Colors.green,
                          fontWeight: FontWeight.bold,
                          fontSize: 16),
                    ),
                    Text(
                      _user.user.lossCount?.toString() ?? "",
                      style: TextStyle(
                          decoration: TextDecoration.none,
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
      alignment: AlignmentDirectional.bottomCenter,
    );
  }

  void populatePlayerStatistics() async {
    String groupId = await _userScoreRepository.getGroupId();
    List<Game> games = await _userScoreRepository.getAllCompletedGames(groupId);

    var totalGames = games.length;
    var secondPosition =
        totalGames - _user.user.winCount - _user.user.lossCount;
    final data = [
      new GameStat(0, _user.user.winCount,
          (_user.user.winCount * 100 / totalGames).ceil()),
      new GameStat(1, _user.user.lossCount,
          (_user.user.lossCount * 100 / totalGames).ceil()),
      new GameStat(
          2, secondPosition, (secondPosition * 100 / totalGames).floor()),
    ];

    setState(() {
      seriesList = [
        new charts.Series<GameStat, int>(
          id: 'Sales',
          domainFn: (GameStat stats, _) => stats.stat,
          measureFn: (GameStat stats, _) => stats.count,
          colorFn: (GameStat stats, _) => getFillColor(stats),
          data: data,
          labelAccessorFn: (GameStat stats, _) => getLabel(stats),
        )
      ];
    });
  }

  charts.Color getFillColor(GameStat stats) {
    if (stats.stat == 0) return charts.MaterialPalette.green.shadeDefault;
    if (stats.stat == 1) return charts.MaterialPalette.red.shadeDefault;
    return charts.MaterialPalette.deepOrange.shadeDefault;
  }

  String getLabel(GameStat stats) {
    if (stats.stat == 0) return "Win ${stats.percentage}%";
    if (stats.stat == 1) return "Lost ${stats.percentage}%";
    return "2nd ${stats.percentage}%";
  }
}

class GameStat {
  int stat;
  int count;
  int percentage;

  GameStat(this.stat, this.count, this.percentage);
}
