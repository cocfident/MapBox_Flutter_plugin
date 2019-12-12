import 'dart:async';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';


const String viewTypeString = 'plugins.platform_mapbox';
typedef void MapBoxViewCreatedCallback(MapBoxViewController controller);
class MapBoxViewController {

  MethodChannel _channel;
  MapBoxViewController.init(int id){
    _channel = new MethodChannel('flutter_mapbox_gray');
  }

  /// List 里是 Map 例如 Map point1 = {'latitude':39.899782,'longtitude':116.393386};
  Future<void> drawLineAction(LineItemData lineItem) async{
    return _channel.invokeListMethod('drawLineAction',lineItem.toJson());
  }



}


class MapboxView extends StatefulWidget {

  final MapBoxViewCreatedCallback onCreated;
  final int zoomLevel; ///地图缩放比例
  final double latitude;///默认维度
  final double longtitude;///默认经度
  final String mapStyleURL;///地图默认样式
  final bool showUserLoction;///是否显示用户位置

  MapboxView({
    Key key,
    this.onCreated,
    this.zoomLevel = 12,
    this.latitude = 39.5427,
    this.longtitude =116.2317,
    this.mapStyleURL = 'mapbox://styles/mapbox/streets-v10',
    this.showUserLoction = true,

  });

  @override
  _MapboxViewState createState() => _MapboxViewState();
}

class _MapboxViewState extends State<MapboxView> {
  @override
  Widget build(BuildContext context) {
    return Container(
      child: _nativeView(),
    );
  }

  Widget _nativeView(){
    if(Platform.isAndroid){
      return Text('我是安卓');
    }else if(Platform.isIOS){
      return UiKitView(
        viewType: viewTypeString,
        onPlatformViewCreated:onPlatformViewCreated,
        creationParams: <String, dynamic>{
          'zoomLevel':widget.zoomLevel,
          'latitude':widget.latitude,
          'longtitude':widget.longtitude,
          'mapStyleURL':widget.mapStyleURL,
          'showUserLoction':widget.showUserLoction,
        },
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
  }

  Future<void> onPlatformViewCreated(id) async {
    if (widget.onCreated == null) {
      return;
    }
    widget.onCreated(new MapBoxViewController.init(id));
  }


}

///数据结构体
///
//划线的数据模型
class LineItemData {
  final List points; ///连线的坐标
  final String lineColor;///线的颜色
  final double lineWidth;///线的宽度
  final double lineOpacity;///线的透明度

  LineItemData({
   @required this.points,
    this.lineColor = '#D75459',
    this.lineWidth = 3.0,
    this.lineOpacity = 0.9,

  });

  Map<String, dynamic> toJson() =>{
    'points':points,
    'lineColor':lineColor,
    'lineWidth':lineWidth,
    'lineOpacity':lineOpacity,
  };

}