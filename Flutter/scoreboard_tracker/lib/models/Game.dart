import 'dart:convert';

import 'User.dart';
import 'UserScore.dart';

class Game {
  String gameId;

  bool isCompleted;

  String scoresJson;

  String winnerId;

  String looserId;

  List<UserScore> userScores;

  Game(this.gameId, this.isCompleted, this.scoresJson, this.winnerId,
      this.looserId) {
    userScores = parseUserScore(scoresJson);
  }

  Game.newGame(List<User> users) {
    this.isCompleted = false;
    this.userScores = new List();
    users.forEach((u) {
      List<int> scores = [null, null, null, null, null, null, null];
      this.userScores.add(new UserScore(u.userId, scores));
    });
  }

  Map<String, dynamic> toJson() {
    List jsonList = List();
    userScores.map((item) => jsonList.add(item.toJson())).toList();

    return {
      'isCompleted': isCompleted,
      'scoresJson': jsonList.toString(),
      'winnerId': winnerId,
      'looserId': looserId
    };
  }

  List<UserScore> parseUserScore(String userScoreString) {
    final parsed = json.decode(userScoreString).cast<Map<String, dynamic>>();
    return parsed.map<UserScore>((json) => UserScore.fromJson(json)).toList();
  }
}
