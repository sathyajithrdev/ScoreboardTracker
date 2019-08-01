import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/foundation.dart';
import 'package:scoreboard_tracker/interfaces/IGameListener.dart';
import 'package:scoreboard_tracker/interfaces/IListener.dart';
import 'package:scoreboard_tracker/models/Game.dart';
import 'package:scoreboard_tracker/models/User.dart';

class UserScoreRepository {
  void getUsers(IListener listener) {
    Firestore.instance.collection('users').getDocuments().then((onValue) {
      List<User> users = new List();
      onValue.documents.forEach((d) => {
            users.add(new User(
                d.data["userId"], d.data["name"], d.data["profileUrl"]))
          });
      listener.onSuccess(users);
    }).catchError((onError) {
      debugPrint(onError.toString());
    });
  }

  void getCurrentOnGoingGame(IGameListener listener) {
    Future<QuerySnapshot> postRef =
        Firestore.instance.collection('groups').getDocuments();

    postRef.then((onValue) {
      String documentId = onValue.documents.first.documentID;
      Firestore.instance
          .collection('groups/$documentId/games')
          .where("isCompleted", isEqualTo: false)
          .getDocuments()
          .then((gamesValue) {
        var onGoingGameDoc = gamesValue.documents.first;
        var onGoingGame = new Game(
            onGoingGameDoc.documentID,
            onGoingGameDoc.data["isCompleted"],
            onGoingGameDoc.data["scoresJson"],
            onGoingGameDoc.data["winnerId"],
            onGoingGameDoc.data["looserId"]);

        listener.onGameSuccess(onGoingGame);
      });
    });
  }
}
