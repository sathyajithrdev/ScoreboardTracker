import 'package:scoreboard_tracker/models/User.dart';

abstract class IListener{
  void onSuccess(List<User> data);
}