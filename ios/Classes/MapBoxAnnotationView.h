//
//  MapBoxAnnotationView.h
//  flutter_mapbox_gray
//
//  Created by 李运洋 on 2019/12/11.
//

#import <Mapbox/Mapbox.h>

NS_ASSUME_NONNULL_BEGIN

@interface MapBoxAnnotationView : MGLAnnotationView

/** <#注释#>*/
@property (nonatomic, copy)  NSString  *titleStr;

@end

NS_ASSUME_NONNULL_END
