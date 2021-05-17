
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterQyPlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_qy_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
