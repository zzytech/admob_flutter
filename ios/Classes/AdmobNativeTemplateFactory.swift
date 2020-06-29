
//
//  AdmobNativeTemplateFactory.swift
//  Runner
//
//  Created by LebranD on 2020/6/28.
//  Copyright © 2020 The Chromium Authors. All rights reserved.
//
import Flutter
import Foundation

class AdmobNativeTemplateFactory : NSObject, FlutterPlatformViewFactory {
    let messeneger: FlutterBinaryMessenger
    
    init(messeneger: FlutterBinaryMessenger) {
        self.messeneger = messeneger;
    }
    
    func create(withFrame frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?) -> FlutterPlatformView {
        return AdmobNativeTemplate(
            frame: frame,
            viewId: viewId,
            args: args as? [String : Any] ?? [:],
            messeneger: messeneger
        );
    }
    
    func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        return FlutterStandardMessageCodec.sharedInstance()
    }
}
