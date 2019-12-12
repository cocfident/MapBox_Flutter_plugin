//
//  MapViewLineModel.h
//  flutter_mapbox_gray
//
//  Created by 李运洋 on 2019/12/11.
//

#import <Foundation/Foundation.h>
#import "MapViewCoordinateModel.h"
#include <MJExtension.h>

NS_ASSUME_NONNULL_BEGIN

@interface MapViewLineModel : NSObject

/** 线的颜色*/
@property (nonatomic, copy)  NSString  *lineColor;
/** 线的宽度*/
@property (nonatomic, assign) CGFloat lineWidth;
/** 线的透明度*/
@property (nonatomic, assign) CGFloat lineOpacity;

/** <#注释#>*/
@property (nonatomic, strong)  NSArray<MapViewCoordinateModel *>  *points;
@end

NS_ASSUME_NONNULL_END
