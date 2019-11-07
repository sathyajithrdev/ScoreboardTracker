import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:scoreboard_tracker/models/Game.dart';
import 'package:scoreboard_tracker/models/User.dart';

class UserScoreRepository {
  Future<List<User>> getUsers() async {
    var userDoc = await Firestore.instance.collection('users').getDocuments();
    List<User> users = new List();
    userDoc.documents?.forEach((d) {
      var data = d.data;
      users.add(new User(data["userId"], data["name"], data["profileUrl"]));
    });
    return users;
  }

  Future<String> getGroupId() async {
    var groupDoc =
        await Firestore.instance.collection('groups').limit(1).getDocuments();
    return groupDoc.documents.length > 0
        ? groupDoc.documents.first.documentID
        : null;
  }

  Future<Game> getCurrentOnGoingGame(String groupId) async {
    var gameDoc = await Firestore.instance
        .collection('groups/$groupId/games')
        .where("isCompleted", isEqualTo: false)
        .limit(1)
        .getDocuments();

    var onGoingGameData = gameDoc.documents.first;
    var data = onGoingGameData.data;
    return new Game(
        onGoingGameData.documentID,
        data["isCompleted"],
        data["scoresJson"],
        data["winnerId"],
        data["looserId"],
        data["timeStamp"]);
  }

  Future<List<Game>> getAllCompletedGames(String groupId) async {
    var gameDoc = await Firestore.instance
        .collection('groups/$groupId/games')
        .where("isCompleted", isEqualTo: true)
        .getDocuments();

    var games = new List<Game>();

    gameDoc.documents.forEach((g) {
      var data = g.data;
      games.add(new Game(g.documentID, data["isCompleted"], data["scoresJson"],
          data["winnerId"], data["looserId"], data["timeStamp"]));
    });
    return games;
  }

  Future<List<Game>> getAllCompletedGamesWithoutDummy(String groupId) async {
    var gameDoc = await Firestore.instance
        .collection('groups/$groupId/games')
        .where("isCompleted", isEqualTo: true)
        .getDocuments();

    var games = new List<Game>();

    gameDoc.documents.forEach((g) {
      var data = g.data;
      if (data["isDummy"] != true) {
        games.add(new Game(
            g.documentID,
            data["isCompleted"],
            data["scoresJson"],
            data["winnerId"],
            data["looserId"],
            data["timeStamp"]));
      }
    });
    return games;
  }

//  Future<void> migratedDataUpdate() async {
//    var gameDoc = await Firestore.instance
//        .collection('groups/VVmSk2oLEPAdPu9agt9T/games')
//        .where("isCompleted", isEqualTo: true)
//        .getDocuments();
//
//    var games = new List<Game>();
//
//    gameDoc.documents.forEach((g) {
//      var data = g.data;
//      games.add(new Game(g.documentID, data["isCompleted"], data["scoresJson"],
//          data["winnerId"], data["looserId"], data["timeStamp"]));
//    });
//
//    var gamesToUpdate = new List<Game>();
//    games.forEach((g) {
//      if (g.userScores.any(isDummyData)) {
//        gamesToUpdate.add(g);
//      }
//    });
//
//    gamesToUpdate.forEach((game) {
//      new Timer(const Duration(seconds: 5), () async {
//        await updateDummydata(game);
//      });
//    });
//  }
//
//  Future<void> updateDummydata(Game game) async {
//    game.timestamp = Timestamp.fromDate(new DateTime(2000));
//    final DocumentReference postRef = Firestore.instance
//        .document('groups/VVmSk2oLEPAdPu9agt9T/games/${game.gameId}');
//
//    await postRef.setData(game.toJsonDummy());
//  }
//
//  bool isDummyData(us) {
//    int totalScore = 0;
//    us.scores.forEach((s) => totalScore += s);
//    return totalScore == 800;
//  }

  Future<void> updateScore(String groupId, Game onGoingGame) async {
    onGoingGame.timestamp = Timestamp.now();
    await Firestore.instance
        .document('groups/$groupId/games/${onGoingGame.gameId}')
        .updateData(onGoingGame.toJson())
        .timeout(new Duration(seconds: 6));
  }

  Future<void> addNewGame(String groupId, Game onGoingGame) async {
    onGoingGame.timestamp = Timestamp.now();

    final CollectionReference postRef =
        Firestore.instance.document('groups/$groupId').collection("games");

    await Firestore.instance.runTransaction((Transaction tx) async {
      await tx.set(postRef.document(), onGoingGame.toJson());
    });
  }
}
