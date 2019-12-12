//
//  MapViewObject.m
//  Runner
//
//  Created by 李运洋 on 2019/12/10.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

#import "MapViewObject.h"
#import <Mapbox/Mapbox.h>
#import "MapViewLineModel.h"
#import "MapBoxAnnotationView.h"


static NSString *const MapBoxAnnotationViewCellId = @"MapBoxAnnotationViewCellId";
@interface MapViewObject ()<MGLMapViewDelegate>
/** channel*/
@property (nonatomic, strong)  FlutterMethodChannel  *channel;
/** 地图*/
@property (nonatomic, strong) MGLMapView *mapView;
@property (nonatomic, strong) MGLPolyline *blueLine;//蓝色线段
/** 大头针数组*/
@property (nonatomic, copy) NSArray *annotationsArray;


///////数据相关
/** 缩放比例*/
@property (nonatomic, assign) int  zoomLevel;
/** 线的颜色*/
@property (nonatomic, strong)  UIColor  *lineColor;
/** 显得宽度*/
@property (nonatomic, assign) CGFloat  lineWidth;
/** 线的透明度*/
@property (nonatomic, assign) CGFloat  lineOpacity;

@end

@implementation MapViewObject
{
    CGRect _frame;
    int64_t _viewId;
    id _args;
   
}

- (id)initWithFrame:(CGRect)frame
  viewId:(int64_t)viewId
    args:(id)args
messager:(NSObject<FlutterBinaryMessenger>*)messenger
{
    if (self = [super init])
    {
        _frame = frame;
        _viewId = viewId;
        _args = args;
        
        NSString *styleUrl = args[@"mapStyleURL"];
        int zoomLevel = [args[@"zoomLevel"] intValue];
        double latitude = [args[@"latitude"] doubleValue];
        double longtitude = [args[@"longtitude"] doubleValue];
        BOOL showUserLoction = [args[@"showUserLoction"] intValue]==1;
        
        self.zoomLevel = zoomLevel;
        
        //设置地图的 frame 和 地图个性化样式
       _mapView = [[MGLMapView alloc] initWithFrame:frame styleURL:[NSURL URLWithString:styleUrl]];
       _mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
       //设置地图默认显示的地点和缩放等级
       [_mapView setCenterCoordinate:CLLocationCoordinate2DMake(latitude, longtitude) zoomLevel:zoomLevel animated:YES];
       //显示用户位置
       _mapView.showsUserLocation  = showUserLoction;
       //定位模式
       _mapView.userTrackingMode   = MGLUserTrackingModeFollow;
       //是否倾斜地图
       _mapView.pitchEnabled       = YES;
       //是否旋转地图
       _mapView.rotateEnabled      = NO;
       //代理
       _mapView.delegate           = self;
        
        
        _channel = [FlutterMethodChannel methodChannelWithName:@"flutter_mapbox_gray" binaryMessenger:messenger];
        
        __weak __typeof__(self) weakSelf = self;

        [_channel setMethodCallHandler:^(FlutterMethodCall * _Nonnull call, FlutterResult  _Nonnull result) {
            [weakSelf onMethodCall:call result:result];
        }];
        
    }
    return self;
}

- (UIView *)view{
    
    return self.mapView;
     
}

#pragma mark -- Flutter 交互监听
-(void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result{
   
   //划线
   if ([[call method] isEqualToString:@"drawLineAction"]) {
       //解析传过来的数据
       MapViewLineModel *model = [MapViewLineModel mj_objectWithKeyValues:call.arguments];
       if (!model.points.count) return;
       self.lineWidth = model.lineWidth;
       self.lineColor = [self colorWithHexString:model.lineColor];
       self.lineOpacity = model.lineOpacity;
       
       
       //添加要划线的点
       CLLocationCoordinate2D coords[model.points.count];
       for (int i = 0; i<model.points.count; i++) {
           MapViewCoordinateModel *coordModel = model.points[i];
            coords[i] = CLLocationCoordinate2DMake(coordModel.latitude, coordModel.longtitude);
       }
       
       [self setannotationsWithArr:model];
       
       return;
       //初始化线
       _blueLine = [MGLPolyline polylineWithCoordinates:coords count:model.points.count];
       ///地图加载完成后绘制 线段
       if ([_mapView.overlays containsObject:self.blueLine]) {//如果已经添加了该条线 那么删除重新添加
           [_mapView removeOverlay:self.blueLine];
       }
       //将线添加到地图上
         [_mapView addOverlay:self.blueLine];
       //设置可见的区域
         [_mapView setVisibleCoordinateBounds:self.blueLine.overlayBounds edgePadding:UIEdgeInsetsMake(50, 50, 50, 50) animated:YES completionHandler:nil];
   }
    
    
}

- (void)setannotationsWithArr:(MapViewLineModel *)model{
    //添加要划线的点
      CLLocationCoordinate2D coords[model.points.count];
      for (int i = 0; i<model.points.count; i++) {
          MapViewCoordinateModel *coordModel = model.points[i];
           coords[i] = CLLocationCoordinate2DMake(coordModel.latitude, coordModel.longtitude);
      }
    
    NSMutableArray *pointsArray = [NSMutableArray array];
    for (NSInteger i = 0; i < model.points.count; ++i) {
        MGLPointAnnotation *pointAnnotation = [[MGLPointAnnotation alloc] init];
        pointAnnotation.coordinate  = coords[i];
        pointAnnotation.title       = @"南锣鼓巷";
        pointAnnotation.subtitle    = @"距离此地9.6km";
        
        [pointsArray addObject:pointAnnotation];
    }
    
    _annotationsArray = [pointsArray copy];
    
     [_mapView addAnnotations:_annotationsArray];
    
    
}


#pragma mark -- mapboxDelegate
- (void)mapViewDidFinishLoadingMap:(MGLMapView *)mapView{
    
    NSLog(@"地图加载完成");
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [_mapView setZoomLevel:self.zoomLevel animated:YES];
    });
    
    
      
}
#pragma mark -- 画线相关 ↓
- (CGFloat)mapView:(MGLMapView *)mapView alphaForShapeAnnotation:(MGLShape *)annotation {
    ///MGLPolyline 和 MGLPolygon 都执行这个方法
    return self.lineOpacity;
}

- (CGFloat)mapView:(MGLMapView *)mapView lineWidthForPolylineAnnotation:(MGLPolyline *)annotation {
    ///MGLPolyline 执行这个方法, MGLPolygon 不执行
    return self.lineWidth;
}

- (UIColor *)mapView:(MGLMapView *)mapView strokeColorForShapeAnnotation:(MGLShape *)annotation {
    
    return self.lineColor;
}

#pragma mark -- 画线相关 ↑
#pragma mark -- 大头针相关 ↓
- (MGLAnnotationView *)mapView:(MGLMapView *)mapView viewForAnnotation:(id<MGLAnnotation>)annotation {
    if (![annotation isKindOfClass:[MGLPointAnnotation class]]) {
        return nil;
    }
    MapBoxAnnotationView *annotationView = [mapView dequeueReusableAnnotationViewWithIdentifier:MapBoxAnnotationViewCellId];
    if (annotationView == nil) {
        annotationView = [[MapBoxAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:MapBoxAnnotationViewCellId];
        
        annotationView.titleStr = @"1";
       
    }
    return annotationView;
}

///是否显示气泡
-(BOOL)mapView:(MGLMapView *)mapView annotationCanShowCallout:(id<MGLAnnotation>)annotation {
    return YES;
}
///完成加载大头针
- (void)mapView:(MGLMapView *)mapView didAddAnnotationViews:(NSArray<MGLAnnotationView *> *)annotationViews {
    [mapView showAnnotations:self.annotationsArray edgePadding:UIEdgeInsetsMake(50, 50, 50, 50) animated:YES completionHandler:nil];
}
///气泡布局

- (UIView *)mapView:(MGLMapView *)mapView leftCalloutAccessoryViewForAnnotation:(id<MGLAnnotation>)annotation{
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 50, 50)];
       view.backgroundColor= [UIColor blueColor];
    return view;
}
- (UIView *)mapView:(MGLMapView *)mapView rightCalloutAccessoryViewForAnnotation:(id<MGLAnnotation>)annotation{
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 50, 50)];
       view.backgroundColor= [UIColor purpleColor];
    return view;
}
///气泡点击
- (void)mapView:(MGLMapView *)mapView tapOnCalloutForAnnotation:(id <MGLAnnotation>)annotation{
    
}


#pragma mark -- 大头针相关 ↑
#pragma mark -- lazy


- (UIColor *)colorWithHexString:(NSString *)color alpha:(CGFloat)alpha
{
    //删除字符串中的空格
    NSString *cString = [[color stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] uppercaseString];
    // String should be 6 or 8 characters
    if ([cString length] < 6)
    {
        return [UIColor clearColor];
    }
    // strip 0X if it appears
    //如果是0x开头的，那么截取字符串，字符串从索引为2的位置开始，一直到末尾
    if ([cString hasPrefix:@"0X"])
    {
        cString = [cString substringFromIndex:2];
    }
    //如果是#开头的，那么截取字符串，字符串从索引为1的位置开始，一直到末尾
    if ([cString hasPrefix:@"#"])
    {
        cString = [cString substringFromIndex:1];
    }
    if ([cString length] != 6)
    {
        return [UIColor clearColor];
    }
    
    // Separate into r, g, b substrings
    NSRange range;
    range.location = 0;
    range.length = 2;
    //r                       截取的range = (0,2)
    NSString *rString = [cString substringWithRange:range];
    //g
    range.location = 2;//     截取的range = (2,2)
    NSString *gString = [cString substringWithRange:range];
    //b
    range.location = 4;//     截取的range = (4,2)
    NSString *bString = [cString substringWithRange:range];
    
    // Scan values
    unsigned int r, g, b;//将字符串十六进制两位数字转为十进制整数
    [[NSScanner scannerWithString:rString] scanHexInt:&r];
    [[NSScanner scannerWithString:gString] scanHexInt:&g];
    [[NSScanner scannerWithString:bString] scanHexInt:&b];
    return [UIColor colorWithRed:((float)r / 255.0f) green:((float)g / 255.0f) blue:((float)b / 255.0f) alpha:alpha];
}
//默认alpha值为1
- (UIColor *)colorWithHexString:(NSString *)color
{
    return [self colorWithHexString:color alpha:1.0f];
}


@end
