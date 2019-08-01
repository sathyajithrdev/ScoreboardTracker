import 'dart:convert';

import 'User.dart';

class UserScore {
  String userId;

  User user;

  List<int> scores;

  UserScore(this.userId, this.scores);



  factory UserScore.fromJson(Map<String, dynamic> parsedJson) {
    var scoresFromJson = parsedJson['scores'];
    var userId = parsedJson['userId'];
    List<int> scoreList = scoresFromJson.cast<int>();
    return new UserScore(userId, scoreList);
  }
}
