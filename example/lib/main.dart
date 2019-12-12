import 'dart:ui';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_mapbox_gray/flutter_mapbox_gray.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  var mapboxViewController;

  @override
  void initState() {
    super.initState();

  }

  @override
  Widget build(BuildContext context) {


    var width = MediaQueryData.fromWindow(window).size.width;

    var height = 300.0;

    MapboxView mapboxView = new  MapboxView(
      onCreated: onMapboxViewCreated,
    );

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Container(
              width: width,
              height: height,
              child: mapboxView,
            ),
            FloatingActionButton(
              onPressed: onDrawline,
            )
          ],
        )
      ),
    );
  }


  void onMapboxViewCreated(mapboxViewController){
    this.mapboxViewController = mapboxViewController;
  }


  void onDrawline(){

    Map point1 = {'latitude':39.899782,'longtitude':116.393386};
    Map point2 = {'latitude':39.903755,'longtitude':116.397724};
    Map point3 = {'latitude':39.939299,'longtitude':116.395628};
    Map point4 = {'latitude':39.940442,'longtitude':116.355471};
    Map point5 = {'latitude':39.908518,'longtitude':116.447093};

    List points = [point1,point2,point3,point4,point5];


    this.mapboxViewController.drawLineAction(LineItemData(points: points));
  }


}


