package com.shatsy.admobflutter

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.ads.mediation.adcolony.AdColonyMediationAdapter
import com.google.android.ads.mediationtestsuite.MediationTestSuite
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.MobileAds
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.logging.MoPubLog
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

fun createAdListener(channel: MethodChannel, getMediationAdapterClassName: () -> String?) : AdListener {
  return object: AdListener() {
    override fun onAdLoaded() {
      Log.e("Google Admob", "mediation adapter class name: ${getMediationAdapterClassName()}")
      channel.invokeMethod("loaded", mapOf("mediationAdapterClassName" to getMediationAdapterClassName()))
    }
    override fun onAdFailedToLoad(errorCode: Int) = channel.invokeMethod("failedToLoad", hashMapOf("errorCode" to errorCode))
    override fun onAdClicked() = channel.invokeMethod("clicked", null)
    override fun onAdImpression() = channel.invokeMethod("impression", null)
    override fun onAdOpened() = channel.invokeMethod("opened", null)
    override fun onAdLeftApplication() = channel.invokeMethod("leftApplication", null)
    override fun onAdClosed() = channel.invokeMethod("closed", null)
  }
}

class AdmobFlutterPlugin(private val context: Context, private val activity: Activity): MethodCallHandler {
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val defaultChannel = MethodChannel(registrar.messenger(), "admob_flutter")
      defaultChannel.setMethodCallHandler(AdmobFlutterPlugin(registrar.context(), registrar.activity()))

      val interstitialChannel = MethodChannel(registrar.messenger(), "admob_flutter/interstitial")
      interstitialChannel.setMethodCallHandler(AdmobInterstitial(registrar))

      val rewardChannel = MethodChannel(registrar.messenger(), "admob_flutter/reward")
      rewardChannel.setMethodCallHandler(AdmobReward(registrar))

      registrar
        .platformViewRegistry()
        .registerViewFactory("admob_flutter/banner", AdmobBannerFactory(registrar.activity(), registrar.messenger()))
      registrar
        .platformViewRegistry()
        .registerViewFactory("admob_flutter/native_template", AdmobNativeTemplateFactory(registrar.activity(), registrar.messenger()))
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method) {
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "initialize" -> {
//        val appId = call.argument<String>("appId")
        MobileAds.initialize(activity) // adcolony 需要用 activity
        // adcolony
        val options = AdColonyMediationAdapter.getAppOptions()
        options.keepScreenOn = true
        options.gdprRequired = true
        // mopub
        val mopubAdUnitId = call.argument<String>("mopubAdUnitId") // 任意有效的广告ID
        MoPub.initializeSdk(context, SdkConfiguration.Builder(mopubAdUnitId!!).withLogLevel(MoPubLog.LogLevel.INFO).build(), null);
      }
      "launchTestSuite" -> {
        val testDevice = call.argument<String>("testDevice")
        if (!testDevice.isNullOrEmpty()) {
          MediationTestSuite.addTestDevice(testDevice)
        }
        MediationTestSuite.launch(activity)
      }
      else -> result.notImplemented()
    }
  }
}
