import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:scoreboard_tracker/interfaces/IListener.dart';
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

  Future<void> updateScore(String groupId, Game onGoingGame) async {
    onGoingGame.timestamp = Timestamp.now();
    final DocumentReference postRef = Firestore.instance
        .document('groups/$groupId/games/${onGoingGame.gameId}');

    Firestore.instance.runTransaction((Transaction tx) async {
      DocumentSnapshot postSnapshot = await tx.get(postRef);
      if (postSnapshot.exists) {
        await tx.update(postRef, onGoingGame.toJson());
      }
    });
  }

  Future<void> addNewGame(String groupId, Game onGoingGame) async {
    onGoingGame.timestamp = Timestamp.now();

    final CollectionReference postRef =
        Firestore.instance.document('groups/$groupId').collection("games");

    await Firestore.instance.runTransaction((Transaction tx) async {
      await tx.set(postRef.document(), onGoingGame.toJson());
    });
  }

  void listenForScoreChanges(
      String groupId, String gameId, IListener listener) async {
    Firestore.instance
        .collection('groups/$groupId/games')
        .where("isCompleted", isEqualTo: false)
        .limit(1)
        .snapshots()
        .listen((onData) {
      listener.onDataChanged();
      onData.documentChanges.forEach((d) {
        var data = d;
      });
    });
  }
}
