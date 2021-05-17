package com.flutter.qy.flutter_qy_plugin_example

import com.flutter.qy.flutter_qy_plugin.FlutterQyPlugin
import io.flutter.app.FlutterApplication

class MainApplication : FlutterApplication() {
    override fun onCreate() {
        super.onCreate()
        val activity = MainActivity::class.java
        FlutterQyPlugin.initSDK(this,"f79970e85bcd857128da6c8390d51b9e",activity)

    }

}