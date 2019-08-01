import 'dart:convert';

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

  List<UserScore> parseUserScore(String userScoreString) {
    final parsed = json.decode(userScoreString).cast<Map<String, dynamic>>();
    return parsed.map<UserScore>((json) => UserScore.fromJson(json)).toList();
  }
}
