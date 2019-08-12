import 'package:fancy_bottom_navigation/fancy_bottom_navigation.dart';
import 'package:flutter/material.dart';
import 'package:scoreboard_tracker/views/HomePage.dart';

import 'views/HistoryPage.dart';
import 'views/StatisticsPage.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Scoreboard Tracker',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MainPage(),
    );
  }
}

class MainPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _MainState();
  }
}

class _MainState extends State<MainPage> {
  int _currentTabIndex = 0;

  final List<Widget> _pages = [
    HomePage(Colors.white),
    StatisticsPage(Colors.deepOrange),
    HistoryPage(Colors.green)
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
//      appBar: AppBar(
//        title: Text('Scoreboard Tracker'),
//      ),
      body: _pages[_currentTabIndex],
      bottomNavigationBar: FancyBottomNavigation(
        tabs: [
          TabData(iconData: Icons.home, title: "Home"),
          TabData(iconData: Icons.book, title: "Statistics"),
          TabData(iconData: Icons.history, title: "History")
        ],
        onTabChangedListener: (position) {
          setState(() {
            _currentTabIndex = position;
          });
        },
      ),
    );
  }

  void onTabTapped(int index) {
    setState(() {
      _currentTabIndex = index;
    });
  }
}
