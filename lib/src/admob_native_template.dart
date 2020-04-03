import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'admob_events.dart';
import 'admob_event_handler.dart';

enum AdmobNativeTemplateType {
  medium,
  small,
}

class AdmobNativeTemplateController extends AdmobEventHandler {
  final MethodChannel _channel;

  AdmobNativeTemplateController(int id, Function(AdmobAdEvent, Map<String, dynamic>) listener)
      : _channel = MethodChannel('admob_flutter/native_template_$id'),
        super(listener) {
    if (listener != null) {
      _channel.setMethodCallHandler(handleEvent);
      _channel.invokeMethod('setListener');
    }
  }

  void dispose() {
    _channel.invokeMethod('dispose');
  }
}

class AdmobNativeTemplate extends StatefulWidget {
  final String adUnitId;
  final AdmobNativeTemplateType type;
  final String testDevice;
  final void Function(AdmobAdEvent, Map<String, dynamic>) listener;
  final void Function(AdmobNativeTemplateController) onNativeTemplateCreated;

  AdmobNativeTemplate({
    Key key,
    @required this.adUnitId,
    @required this.type,
    this.testDevice,
    this.listener,
    this.onNativeTemplateCreated,
  }) : assert(type != null), super(key: key);

  @override
  _AdmobNativeTemplateState createState() => _AdmobNativeTemplateState();
}

class _AdmobNativeTemplateState extends State<AdmobNativeTemplate> {
  AdmobNativeTemplateController _controller;
  final Key _platformKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return Container(
        width: double.infinity,
        height: widget.type == AdmobNativeTemplateType.medium ? 350 : 100,
        child: AndroidView(
          viewType: 'admob_flutter/native_template',
          creationParams: <String, dynamic>{
            'adUnitId': widget.adUnitId,
            'type': '${widget.type}'.split('.')[1],
            ...(widget.testDevice?.isNotEmpty ?? false) ? <String, dynamic>{
              'testDevice': widget.testDevice,
            } : <String, dynamic>{},
          },
          creationParamsCodec: StandardMessageCodec(),
          onPlatformViewCreated: _onPlatformViewCreated,
        ),
      );
    } else if (defaultTargetPlatform == TargetPlatform.iOS) {
      return Container(
        width: double.infinity,
        height: widget.type == AdmobNativeTemplateType.medium ? 350 : 300,
        child: UiKitView(
          key: _platformKey,
          viewType: 'admob_flutter/native_template',
          creationParams: <String, dynamic>{
            'adUnitId': widget.adUnitId,
            'type': '${widget.type}'.split('.')[1],
            ...(widget.testDevice?.isNotEmpty ?? false) ? <String, dynamic>{
              'testDevice': widget.testDevice,
            } : <String, dynamic>{},
          },
          creationParamsCodec: StandardMessageCodec(),
          onPlatformViewCreated: _onPlatformViewCreated,
        ),
      );
    } else {
      return Text('$defaultTargetPlatform is not yet supported by the plugin');
    }
  }

  @override
  void dispose() {
    super.dispose();
  }

  void _onPlatformViewCreated(int id) {
    _controller = AdmobNativeTemplateController(id, widget.listener);

    if (widget.onNativeTemplateCreated != null) {
      widget.onNativeTemplateCreated(_controller);
    }
  }
}
