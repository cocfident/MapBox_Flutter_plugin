import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_mapbox_gray/flutter_mapbox_gray.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_mapbox_gray');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

//  test('getPlatformVersion', () async {
//    expect(await MapViewController.platformVersion, '42');
//  });


}
