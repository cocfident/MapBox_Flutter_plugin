//
//  MapViewCoordinateModel.h
//  flutter_mapbox_gray
//
//  Created by 李运洋 on 2019/12/11.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MapViewCoordinateModel : NSObject
/** 维度*/
@property (nonatomic, assign) double  latitude;
/** 经度*/
@property (nonatomic, assign) double  longtitude;
@end

NS_ASSUME_NONNULL_END
