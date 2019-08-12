import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:lottie_flutter/lottie_flutter.dart';

class LoadingView extends StatefulWidget {
  LoadingView({Key key}) : super(key: key);

  @override
  State<LoadingView> createState() {
    return LoadingViewState();
  }
}

class LoadingViewState extends State<LoadingView>
    with SingleTickerProviderStateMixin {
  String _loadingMessage = "";
  AnimationController _controller;
  LottieComposition _composition;

  @override
  void initState() {
    super.initState();
    _prepareLottieAnimation();
  }

  @override
  Widget build(BuildContext context) {
    return new Container(
      padding: EdgeInsets.all(0),
      decoration: new BoxDecoration(
        image: new DecorationImage(
          image: new AssetImage("assets/images/background.jpg"),
          fit: BoxFit.fill,
        ),
      ),
      child: Center(
        child: Stack(
            alignment: AlignmentDirectional.bottomCenter,
            children: <Widget>[
              new Lottie(
                composition: _composition,
                size: const Size(400.0, 400.0),
                controller: _controller,
              ),
            ]),
      ),
    );
  }

  Future<void> _prepareLottieAnimation() async {
    _controller = new AnimationController(
      duration: const Duration(milliseconds: 2000),
      vsync: this,
    );

    await loadAsset("assets/images/won_anim.json")
        .then((LottieComposition composition) {
      setState(() {
        _composition = composition;
        _controller.reset();
      });
    });
  }

  Future<LottieComposition> loadAsset(String assetName) async {
    return await rootBundle
        .loadString(assetName)
        .then<Map<String, dynamic>>((String data) => json.decode(data))
        .then((Map<String, dynamic> map) => new LottieComposition.fromMap(map));
  }

  Future<void> showLoading(String message) async {
    setState(() {
      _loadingMessage = message;
    });
    _prepareLottieAnimation();
  }

  void hideLoading() {
//    setState(() {
//      _controller.stop();
//    });
  }
}
