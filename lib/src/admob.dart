import 'package:flutter/services.dart';

class Admob {
  Admob._();

  static const MethodChannel _channel = MethodChannel('admob_flutter');

  static Future<void> initialize(String appId) {
    return _channel.invokeMethod('initialize', appId);
  }

  static Future<void> launchTestSuite({String testDevice}) {
    return _channel.invokeMethod(
      'launchTestSuite',
      (testDevice?.isNotEmpty ?? false) ? <String, dynamic>{
        'testDevice': testDevice
      } : <String, dynamic>{},
    );
  }
}
