#import <Flutter/Flutter.h>

@interface FlutterQyPlugin : NSObject<FlutterPlugin>
@property (nonatomic, retain) FlutterMethodChannel *channel;
@property (nonatomic, retain) UIViewController *viewController;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar;

@end
