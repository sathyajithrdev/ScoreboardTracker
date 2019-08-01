import 'package:scoreboard_tracker/models/Game.dart';

abstract class IGameListener{
  void onGameSuccess(Game data);
}