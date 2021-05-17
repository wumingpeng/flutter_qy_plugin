package com.flutter.qy.flutter_qy_plugin_example

import android.content.Intent
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity() {
    var methodChannel: MethodChannel? = null
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e("Notification", "Notification>>>>>>>")
//        val intent = getIntent()
        methodChannel = MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, "toFlutterChannelName") //1
        if (this.methodChannel != null) {
            this.methodChannel?.invokeMethod("fluMethod", "我是原生Android，我将参数传递给Flutter里面的一个方法", object : MethodChannel.Result {
                override fun success(o: Any?) {}
                override fun error(s: String, s1: String?, o: Any?) {}
                override fun notImplemented() {}
            })
        }
        if (intent.hasExtra("com.qiyukf.nim.EXTRA.NOTIFY_CONTENT")) {
            Log.e("Notification", "Notification>>>>>>>")
        }
    }
}
