//
//  AdmobNativeBanner.swift
//  Runner
//
//  Created by LebranD on 2020/6/18.
//  Copyright © 2020 The Chromium Authors. All rights reserved.
//

import Foundation
import Flutter
import GoogleMobileAds

class AdmobNativeTemplate : NSObject, FlutterPlatformView {
    private let channel : FlutterMethodChannel
    private var adView : GADUnifiedNativeAdView?
    private let messeneger : FlutterBinaryMessenger
    private let frame : CGRect
    private let viewId : Int64
    private let args : [String : Any]
    private var adLoader : GADAdLoader?
    
    
    init(frame: CGRect, viewId: Int64, args: [String: Any], messeneger: FlutterBinaryMessenger) {
        self.args = args
        self.messeneger = messeneger
        self.frame = frame
        self.viewId = viewId
        channel = FlutterMethodChannel(name: "admob_flutter/native_template_\(viewId)", binaryMessenger: messeneger)
    }
    
    func view() -> UIView {
        return getNativeTemplate() ?? UIView();
    }
    
    fileprivate func dispose() {
        adView?.removeFromSuperview()
        adView = nil
        channel.setMethodCallHandler(nil)
    }
    
    fileprivate func getNativeTemplate() -> GADUnifiedNativeAdView? {
        if adView == nil {
            let bundle: Bundle? = Bundle.init(path: Bundle.main.path(forResource: "Admob", ofType: "bundle") ?? "")
            adView = (bundle?.loadNibNamed("UnifiedNativeAdView", owner: self, options: nil)?.last as! GADUnifiedNativeAdView) ;
            adView!.frame = self.frame.width == 0 ? CGRect(x: 0, y: 0, width: 1, height: 1) : self.frame;
            
            let adOptions : GADNativeAdViewAdOptions = GADNativeAdViewAdOptions();
            adOptions.preferredAdChoicesPosition = .topRightCorner; //广告选择图标位置
            
            let mediaOptions : GADNativeAdMediaAdLoaderOptions = GADNativeAdMediaAdLoaderOptions();
            mediaOptions.mediaAspectRatio = .landscape; //宽高比 16:9
            
            adLoader = GADAdLoader(
                adUnitID: self.args["adUnitId"] as? String ?? "ca-app-pub-3940256099942544/3986624511",
                rootViewController: UIApplication.shared.keyWindow?.rootViewController,
                adTypes: [ .unifiedNative ],
                options: [ adOptions, mediaOptions ]
            );
                        
            channel.setMethodCallHandler { [weak self] (flutterMethodCall: FlutterMethodCall, flutterResult: FlutterResult) in
                switch flutterMethodCall.method {
                case "setListener":
                    self?.adLoader?.delegate = self
                    break
                case "dispose":
                    self?.dispose()
                    break
                default:
                    flutterResult(FlutterMethodNotImplemented)
                }
            }
            print("adView create success \(String(describing: adView?.description))");
            
            adLoader?.load(GADRequest());
        }
        return adView;
    }
}

extension AdmobNativeTemplate : GADUnifiedNativeAdLoaderDelegate {
    func adLoader(_ adLoader: GADAdLoader, didFailToReceiveAdWithError error: GADRequestError) {
        channel.invokeMethod("failedToLoad", arguments: [
            "errorCode": error.code,
            "error": error.localizedDescription
        ])
    }
    func adLoader(_ adLoader: GADAdLoader, didReceive nativeAd: GADUnifiedNativeAd) {
        channel.invokeMethod("loaded", arguments: nil)
        self.adView?.nativeAd = nativeAd;
        nativeAd.delegate = self;
        self.adView?.subviews.last(where: { (view) -> Bool in
            return view.tag == 10001;
        })?.isHidden = true;
        
        (self.adView?.headlineView as? UILabel)?.text = nativeAd.headline;
        
        self.adView?.mediaView?.mediaContent = nativeAd.mediaContent;
        
        if nativeAd.mediaContent.hasVideoContent {
            nativeAd.mediaContent.videoController.delegate = self
        }
        
        (self.adView?.bodyView as? UILabel)?.text = nativeAd.body;
        self.adView?.bodyView?.isHidden = nativeAd.body == nil;
        
        (self.adView?.callToActionView as? UIButton)?.setTitle(nativeAd.callToAction, for: .normal);
        self.adView?.callToActionView?.isHidden = nativeAd.callToAction == nil;
        
        (self.adView?.iconView as? UIImageView)?.image = nativeAd.icon?.image;
        self.adView?.iconView?.isHidden = nativeAd.icon == nil;
        
        (self.adView?.starRatingView as? UIImageView)?.image = imageForStars(numberOfStars: nativeAd.starRating ?? NSDecimalNumber(floatLiteral: 0));
        self.adView?.starRatingView?.isHidden = nativeAd.starRating == nil;
        
        (self.adView?.storeView as? UILabel)?.text = nativeAd.store;
        self.adView?.storeView?.isHidden = nativeAd.store == nil;
        
        (self.adView?.priceView as? UILabel)?.text = nativeAd.price;
        self.adView?.priceView?.isHidden = nativeAd.price == nil;
        
        (self.adView?.advertiserView as? UILabel)?.text = nativeAd.advertiser;
        self.adView?.advertiserView?.isHidden = nativeAd.advertiser == nil;
        
        self.adView?.callToActionView?.isUserInteractionEnabled = false;
        
        print("Native adapter class name: \(nativeAd.responseInfo.adNetworkClassName ?? "")"); //广告联盟类名称
    }
    
    func imageForStars(numberOfStars:  NSDecimalNumber) -> UIImage? {
        let starRating = numberOfStars.doubleValue;
        let bundle: Bundle? = Bundle.init(path: Bundle.main.path(forResource: "Admob", ofType: "bundle") ?? "")
        if starRating >= 5 {
            return UIImage(named: "stars_5", in: bundle, compatibleWith: nil);
        } else if (starRating >= 4.5) {
            return UIImage(named: "stars_4_5", in: bundle, compatibleWith: nil);
        } else if (starRating >= 4) {
            return UIImage(named: "stars_4", in: bundle, compatibleWith: nil);
        } else if (starRating >= 3.5) {
            return UIImage(named: "stars_3_5", in: bundle, compatibleWith: nil);
        } else {
            return nil;
        }
    }
}

extension AdmobNativeTemplate : GADUnifiedNativeAdDelegate {
    // The native ad was shown.
    func nativeAdDidRecordImpression(_ nativeAd: GADUnifiedNativeAd) {
        
    }
    
    // The native ad was clicked on.
    func nativeAdDidRecordClick(_ nativeAd: GADUnifiedNativeAd) {
        channel.invokeMethod("clicked", arguments: nil)
    }
    
    // The native ad will present a full screen view.
    func nativeAdWillPresentScreen(_ nativeAd: GADUnifiedNativeAd) {
        channel.invokeMethod("opened", arguments: nil)
    }
    
    // The native ad will dismiss a full screen view.
    func nativeAdWillDismissScreen(_ nativeAd: GADUnifiedNativeAd) {
    }
    
    // The native ad did dismiss a full screen view.
    func nativeAdDidDismissScreen(_ nativeAd: GADUnifiedNativeAd) {
        channel.invokeMethod("closed", arguments: nil)
    }
    
    // The native ad will cause the application to become inactive and open a new application.
    func nativeAdWillLeaveApplication(_ nativeAd: GADUnifiedNativeAd) {
        channel.invokeMethod("leftApplication", arguments: nil)
    }
}

extension AdmobNativeTemplate : GADVideoControllerDelegate {
    func videoControllerDidEndVideoPlayback(_ videoController: GADVideoController) {
        
    }
    func videoControllerDidMuteVideo(_ videoController: GADVideoController) {
        
    }
    func videoControllerDidPlayVideo(_ videoController: GADVideoController) {
        
    }
    func videoControllerDidPauseVideo(_ videoController: GADVideoController) {
        
    }
    func videoControllerDidUnmuteVideo(_ videoController: GADVideoController) {
        
    }
}
