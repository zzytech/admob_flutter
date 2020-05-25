package com.shatsy.admobflutter

import android.content.Context
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class AdmobBannerFactory(private val registrar: PluginRegistry.Registrar): PlatformViewFactory(StandardMessageCodec.INSTANCE) {
  override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
    return AdmobBanner(registrar.activity(), registrar.messenger(), viewId, args as HashMap<*, *>?)
  }
}