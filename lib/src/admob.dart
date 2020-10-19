import 'package:flutter/services.dart';

class Admob {
  Admob._();

  static const MethodChannel _channel = MethodChannel('admob_flutter');

  static void initialize(String appId) {
    _channel.invokeMethod('initialize', <String, dynamic> {
      'appId': appId,
    });
  }

  static void launchTestSuite({String testDevice}) {
    _channel.invokeMethod(
      'launchTestSuite',
      (testDevice?.isNotEmpty ?? false)
          ? <String, dynamic>{
              'testDevice': testDevice,
            }
          : <String, dynamic>{},
    );
  }
}
