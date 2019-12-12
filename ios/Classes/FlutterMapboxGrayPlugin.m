#import "FlutterMapboxGrayPlugin.h"
#import "MapBoxViewFactory.h"
@implementation FlutterMapboxGrayPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
//  FlutterMethodChannel* channel = [FlutterMethodChannel
//      methodChannelWithName:@"flutter_mapbox_gray"
//            binaryMessenger:[registrar messenger]];
//  FlutterMapboxGrayPlugin* instance = [[FlutterMapboxGrayPlugin alloc] init];
//  [registrar addMethodCallDelegate:instance channel:channel];
    
    
    MapBoxViewFactory *mapboxFactory = [[MapBoxViewFactory alloc] initWithMessenger:registrar.messenger];
    
    [registrar registerViewFactory:mapboxFactory withId:@"plugins.platform_mapbox"];
    
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
