package com.shatsy.admobflutter

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

class AdmobInterstitial(private val registrar: PluginRegistry.Registrar): MethodChannel.MethodCallHandler {
  companion object {
    val allAds: MutableMap<Int, InterstitialAd> = mutableMapOf()
  }
  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when(call.method) {
      "setListener" -> {
        val id = call.argument<Int>("id")
        if (allAds[id]!!.adListener != null) return

        val adChannel = MethodChannel(registrar.messenger(), "admob_flutter/interstitial_$id")
        allAds[id]!!.adListener = createAdListener(adChannel, fun():String? = allAds[id]!!.mediationAdapterClassName)
        result.success(null)
      }
      "load" -> {
        val id = call.argument<Int>("id")
        val adUnitId = call.argument<String>("adUnitId")
        val testDevice = call.argument<String>("testDevice")
        val adRequest = AdRequest.Builder().addTestDevice(testDevice ?: "").build()

        if (allAds[id] == null) {
          allAds[id!!] = InterstitialAd(registrar.activity())
          allAds[id]!!.adUnitId = adUnitId
        }
        allAds[id]?.loadAd(adRequest)
        result.success(null)
      }
      "isLoaded" -> {
        val id = call.argument<Int>("id")

        if (allAds[id] == null) {
          result.success(false)
          return
        }

        if (allAds[id]!!.isLoaded) {
          result.success(true)
        } else {
          result.success(false)
        }
      }
      "show" -> {
        val id = call.argument<Int>("id")

        if (allAds[id]!!.isLoaded) {
          allAds[id]!!.show()
          result.success(null)
        } else {
          result.error(null, null, null)
        }
      }
      "dispose" -> {
        val id = call.argument<Int>("id")

        allAds.remove(id)
        result.success(null)
      }
      else -> result.notImplemented()
    }
  }
}
