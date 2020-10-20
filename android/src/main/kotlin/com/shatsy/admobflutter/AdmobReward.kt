package com.shatsy.admobflutter

import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

class AdmobReward(private val registrar: PluginRegistry.Registrar): MethodChannel.MethodCallHandler {
  companion object {
    val allAds: MutableMap<Int, RewardedVideoAd> = mutableMapOf()
  }

  lateinit var adChannel: MethodChannel

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when(call.method) {
      "setListener" -> {
        val id = call.argument<Int>("id")
        if (allAds[id]!!.rewardedVideoAdListener != null) return

        adChannel = MethodChannel(registrar.messenger(), "admob_flutter/reward_$id")
        allAds[id]!!.rewardedVideoAdListener = object: RewardedVideoAdListener {
          override fun onRewardedVideoAdClosed() {
//            Log.e("Google Admob", "Reward close ~~~")
            adChannel.invokeMethod("closed", null)
          }
          override fun onRewardedVideoAdLeftApplication() = adChannel.invokeMethod("leftApplication", null)
          override fun onRewardedVideoAdLoaded() {
            Log.e("Google Admob", "mediation adapter class name: ${allAds[id]!!.mediationAdapterClassName}")
            adChannel.invokeMethod("loaded", mapOf("mediationAdapterClassName" to allAds[id]!!.mediationAdapterClassName))
          }
          override fun onRewardedVideoAdOpened() = adChannel.invokeMethod("opened", null)
          override fun onRewardedVideoCompleted() {
//            Log.e("Google Admob", "Reward complete ~~~")
            adChannel.invokeMethod("completed", null)
          }
          override fun onRewarded(reward: RewardItem?) {
//            Log.e("Google Admob", "Reward reward ~~~")
            adChannel.invokeMethod("rewarded", hashMapOf("type" to (reward?.type ?: ""), "amount" to (reward?.amount ?: 0)))
          }
          override fun onRewardedVideoStarted() = adChannel.invokeMethod("started", null)
          override fun onRewardedVideoAdFailedToLoad(errorCode: Int) = adChannel.invokeMethod("failedToLoad", hashMapOf("errorCode" to errorCode))
        }
        result.success(null)
      }
      "load" -> {
        val id = call.argument<Int>("id")
        val adUnitId = call.argument<String>("adUnitId")
        val testDevice = call.argument<String>("testDevice")
        val userId = call.argument<String>("userId")
        val customData = call.argument<String>("customData")
        val adRequest = AdRequest.Builder().addTestDevice(testDevice ?: "")
                .build()

        if (allAds[id] == null) allAds[id!!] = MobileAds.getRewardedVideoAdInstance(registrar.activity())
        allAds[id]?.userId = userId
        allAds[id]?.customData = customData
        allAds[id]?.loadAd(adUnitId, adRequest)
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

        allAds[id]!!.destroy(registrar.context())
        allAds.remove(id)
        result.success(null)
      }
      else -> result.notImplemented()
    }
  }
}
