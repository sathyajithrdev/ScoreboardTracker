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
    List<int> scores = new List();

    var scoreList;
    if (scoresFromJson is String) {
      scoreList = json.decode(scoresFromJson);
    } else {
      scoreList = scoresFromJson;
    }
    scoreList.forEach((s) {
      scores.add((s as int));
    });

    return new UserScore(userId, scores);
  }

  String toJson() {
    return jsonEncode({'userId': userId, 'scores': scores});
  }
}
