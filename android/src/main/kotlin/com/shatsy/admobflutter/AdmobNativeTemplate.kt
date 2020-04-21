package com.shatsy.admobflutter

import android.content.Context
import android.view.View
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.util.concurrent.atomic.AtomicBoolean

/**
 * medium - 350dp
 * small -
 */
class AdmobNativeTemplate (private val context: Context, private val messenger: BinaryMessenger, private val id: Int, private val args: HashMap<*, *>?) : PlatformView, MethodChannel.MethodCallHandler {
    private val channel: MethodChannel = MethodChannel(messenger, "admob_flutter/native_template_$id")
    private val adView: TemplateView
    private val adLoader: AdLoader

    private var adListener: AdListener? = null
    private val adLoaded: AtomicBoolean = AtomicBoolean(false)

    init {
        channel.setMethodCallHandler(this)

        val type = args?.get("type") as String?
        adView = when (type) {
            "medium" -> {
                View.inflate(context, R.layout.admob_medium_template_view, null) as TemplateView
            }
            else -> {
                View.inflate(context, R.layout.admob_small_template_view, null) as TemplateView
            }
        }
        val adUnitId = args?.get("adUnitId") as String?
        adLoader = AdLoader.Builder(context, adUnitId)
                .forUnifiedNativeAd {
                    it.let { nativeAd ->
                        adLoaded.set(nativeAd != null)
                        adView?.setNativeAd(nativeAd)
                    }
                }
                .withAdListener(object : AdListener() {
                    override fun onAdImpression() {
                        adListener?.onAdImpression()
                    }

                    override fun onAdLeftApplication() {
                        adListener?.onAdLeftApplication()
                    }

                    override fun onAdClicked() {
                        adListener?.onAdClicked()
                    }

                    override fun onAdFailedToLoad(p0: Int) {
                        adListener?.onAdFailedToLoad(p0)
                    }

                    override fun onAdClosed() {
                        adListener?.onAdClosed()
                    }

                    override fun onAdOpened() {
                        adListener?.onAdOpened()
                    }

                    override fun onAdLoaded() {
                        adListener?.onAdLoaded()
                    }
                })
                .build()
        val testDevice = args?.get("testDevice") as String?
        val adRequest = AdRequest.Builder().addTestDevice(testDevice ?: "").build()
        adLoader.loadAd(adRequest)
    }

    override fun getView(): View {
        return adView
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call.method) {
            "setListener" -> adListener = createAdListener(channel, fun():String? = adView.mediationAdapterClassName)
            "dispose" -> dispose()
            else -> result.notImplemented()
        }
    }

    override fun dispose() {
        adListener = null
        adView?.visibility = View.GONE
        if (adLoaded.get()) {
            adView?.destroyNativeAd()
        }
        channel.setMethodCallHandler(null)
    }
}
