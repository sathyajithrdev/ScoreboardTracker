import 'dart:convert';
import 'package:cloud_firestore/cloud_firestore.dart';

import 'User.dart';
import 'UserScore.dart';

class Game {
  String gameId;

  bool isCompleted;

  String scoresJson;

  String winnerId;

  String looserId;

  Timestamp timestamp;

  List<UserScore> userScores;

  Game(this.gameId, this.isCompleted, this.scoresJson, this.winnerId,
      this.looserId, this.timestamp) {
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
      'looserId': looserId,
      'timeStamp': timestamp
    };
  }

  Map<String, dynamic> toJsonDummy() {
    List jsonList = List();
    userScores.map((item) => jsonList.add(item.toJson())).toList();

    return {
      'isCompleted': isCompleted,
      'scoresJson': jsonList.toString(),
      'winnerId': winnerId,
      'looserId': looserId,
      'timeStamp': timestamp,
      'isDummy': true
    };
  }

  List<UserScore> parseUserScore(String userScoreString) {
    final parsed = json.decode(userScoreString).cast<Map<String, dynamic>>();
    return parsed.map<UserScore>((json) => UserScore.fromJson(json)).toList();
  }
}
