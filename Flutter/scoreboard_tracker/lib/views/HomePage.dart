import 'package:flutter/material.dart';
import 'package:scoreboard_tracker/interfaces/IGameListener.dart';
import 'package:scoreboard_tracker/interfaces/IListener.dart';
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

class _HomePageState extends State<HomePage>
    implements IListener, IGameListener {
  List<User> _users = new List();
  Game _onGoingGame;

  UserScoreRepository _userRepository;

  _HomePageState() {
    _userRepository = new UserScoreRepository();
    _userRepository.getUsers(this);
  }

  @override
  void onSuccess(List<User> data) {
    setState(() {
      _users = data;
    });

    _userRepository.getCurrentOnGoingGame(this);
  }

  @override
  void onGameSuccess(Game data) {
    data.userScores.forEach((s) => {
          s.user = _users.firstWhere((u) {
            return u.userId == s.userId;
          })
        });

    setState(() {
      _onGoingGame = data;
    });
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: _onGoingGame?.userScores?.length ?? 0,
      itemBuilder: (context, index) {
        return getUserCard(_onGoingGame.userScores[index]);
      },
    );
  }

  Card getUserCard(UserScore user) {
    return Card(
      elevation: 6,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(6.0)),
      ),
      margin: const EdgeInsets.all(8),
      child: Row(
        children: <Widget>[
          new Stack(
            alignment: AlignmentDirectional.bottomStart,
            children: <Widget>[
              Image.network(
                "https://photogallery.indiatimes.com/photo/64290957.cms",
                width: 150,
                height: 250,
                fit: BoxFit.cover,
              ),
              Container(
                color: Color.fromARGB(140, 10, 10, 10),
                width: 150,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: <Widget>[
                    Padding(
                      child: Text(
                        user.user.userName,
                        style: TextStyle(color: Colors.red),
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
                            user.user.lossCount?.toString() ?? "",
                            style: TextStyle(color: Colors.red),
                          ),
                          Text(
                            user.user.winCount?.toString() ?? "",
                            style: TextStyle(color: Colors.green),
                          ),
                        ],
                      ),
                      padding: EdgeInsets.all(6),
                    )
                  ],
                ),
              )
            ],
          ),
          Expanded(
            child: GridView.count(
              // Create a grid with 2 columns. If you change the scrollDirection to
              // horizontal, this produces 2 rows.
              crossAxisCount: 3,
              shrinkWrap: true,
              crossAxisSpacing: 0,
              mainAxisSpacing: 0,
              padding: EdgeInsets.all(0),
              // Generate 100 widgets that display their index in the List.
              children: List.generate(8, (index) {
                return (getUserScoreEdit(index, user));
              }),
            ),
          )
        ],
      ),
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
          controller: new TextEditingController.fromValue(new TextEditingValue(
              text: (userScore.scores[index] ?? "").toString(),
              selection: new TextSelection.collapsed(
                  offset:
                      userScore.scores[index]?.toString()?.length ?? 1 - 1))),
        ),
      );

    return Padding(
      padding: EdgeInsets.all(12),
      child: Text(getTotalScore(userScore)),
    );
  }

  void updateScore(int index, String text, UserScore userScore) {
    setState(() {
      userScore.scores[index] =
          ObjectUtil.isNullOrEmpty(text) ? null : int.parse(text);
    });
  }

  String getTotalScore(UserScore userScore) {
    int total = 0;
    userScore.scores.forEach((s) => total += s ?? 0);
    return total.toString();
  }
}
