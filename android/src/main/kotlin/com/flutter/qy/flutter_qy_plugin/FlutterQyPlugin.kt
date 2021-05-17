package com.flutter.qy.flutter_qy_plugin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
//import com.qiyukf.nimlib.sdk.RequestCallback
//import com.qiyukf.nimlib.sdk.StatusBarNotificationConfig
import com.qiyukf.unicorn.api.*
import com.qiyukf.unicorn.api.lifecycle.SessionLifeCycleOptions
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*

/** FlutterQyPlugin */
class FlutterQyPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private val CHANNEL_NAME: String? = "flutter_qiyu"
    private lateinit var activity: Activity

    companion object {
        fun initSDK(context: Context, appKey: String?, activity: Any) {
            val ysfOptions = YSFOptions()
//            val statusBarNotificationConfig = StatusBarNotificationConfig()
//            statusBarNotificationConfig.notificationEntrance = activity as Class<out Activity>
//            ysfOptions.statusBarNotificationConfig = statusBarNotificationConfig
            ysfOptions.onBotEventListener = object : OnBotEventListener() {
                override fun onUrlClick(context: Context, url: String): Boolean {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                    return true
                }
            }
            // 如果项目中使用了 Glide 可以通过设置 gifImageLoader 去加载 gif 图片
            ysfOptions.gifImageLoader = GlideGifImagerLoader(context)
            Unicorn.init(context.applicationContext, appKey, ysfOptions, GlideImageLoader(context))
        }
    }


    /**
     * Plugin registration.
     */
    fun registerWith(registrar: Registrar) {
        val plugin = FlutterQyPlugin()
        plugin.setupChannel(registrar.messenger(), registrar.activeContext())
    }

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private var channel: MethodChannel? = null

    private var context: Context? = null
    private var ysfOptions: YSFOptions? = null
    private val unreadCountChangeListener = UnreadCountChangeListener { unreadCount ->
        val map: MutableMap<String, Any> = HashMap()
        map["unreadCount"] = unreadCount
        channel!!.invokeMethod("onUnreadCountChange", map)
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
        setupChannel(flutterPluginBinding.binaryMessenger, flutterPluginBinding.applicationContext)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        teardownChannel()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android " + Build.VERSION.RELEASE)
        } else if (call.method == "registerApp") {
            val appKey = call.argument<String>("appKey")
            val appName = call.argument<String>("appName")
            registerApp(appKey, appName)
            result.success(true)
        } else if (call.method == "openServiceWindow") {
            openServiceWindow(call)
            result.success(true)
        } else if (call.method == "setCustomUIConfig") {
            setCustomUIConfig(call)
            result.success(true)
        } else if (call.method == "getUnreadCount") {
            getUnreadCount(call, result)
        } else if (call.method == "setUserInfo") {
            setUserInfo(call)
        } else if (call.method == "logout") {
            logout()
        } else if (call.method == "cleanCache") {
            cleanCache()
        } else {
            result.notImplemented()
        }
    }

    private fun registerApp(appKey: String?, appName: String?) {
        if (ysfOptions == null) {
            ysfOptions = YSFOptions()
//            val statusBarNotificationConfig = StatusBarNotificationConfig()
//            ysfOptions!!.statusBarNotificationConfig = statusBarNotificationConfig
            ysfOptions!!.onBotEventListener = object : OnBotEventListener() {
                override fun onUrlClick(context: Context, url: String): Boolean {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                    return true
                }
            }
            // 如果项目中使用了 Glide 可以通过设置 gifImageLoader 去加载 gif 图片
            ysfOptions!!.gifImageLoader = GlideGifImagerLoader(context)
        }
        Unicorn.init(context!!.applicationContext, appKey, ysfOptions, GlideImageLoader(context))
        Unicorn.addUnreadCountChangeListener(unreadCountChangeListener, true)
    }

    private fun openServiceWindow(call: MethodCall) {
        val sourceMap = call.argument<Map<*, *>>("source")!!
        val commodityInfoMap = call.argument<Map<*, *>>("commodityInfo")
        val sourceTitle = sourceMap["sourceTitle"] as String?
        val sourceUrl = sourceMap["sourceUrl"] as String?
        val sourceCustomInfo = sourceMap["sourceCustomInfo"] as String?
        var productDetail: ProductDetail? = null
        if (commodityInfoMap != null) {
            val commodityInfoTitle = commodityInfoMap["commodityInfoTitle"] as String?
            val commodityInfoDesc = commodityInfoMap["commodityInfoDesc"] as String?
            val pictureUrl = commodityInfoMap["pictureUrl"] as String?
            val commodityInfoUrl = commodityInfoMap["commodityInfoUrl"] as String?
            val note = commodityInfoMap["note"] as String?
            var show = false
            var alwaysSend = true
            if (commodityInfoMap.containsKey("show")) show = commodityInfoMap["show"] as Boolean
            if (commodityInfoMap.containsKey("alwaysSend")) alwaysSend = commodityInfoMap["alwaysSend"] as Boolean
            var sendByUser = false
            if (commodityInfoMap.containsKey("sendByUser")) sendByUser = commodityInfoMap["sendByUser"] as Boolean
            productDetail = ProductDetail.Builder()
                    .setTitle(commodityInfoTitle)
                    .setDesc(commodityInfoDesc)
                    .setPicture(pictureUrl)
                    .setUrl(commodityInfoUrl)
                    .setNote(note)
                    .setShow(if (show) 1 else 0)
                    .setAlwaysSend(alwaysSend)
                    .setSendByUser(sendByUser)
                    .build()
        }
        val sessionTitle = call.argument<String>("sessionTitle")
        val groupId = call.argument<Any>("groupId") as Int
        val staffId = call.argument<Any>("staffId") as Int
        val robotId = call.argument<Any>("robotId") as Int
        var robotFirst = false
        if (call.hasArgument("robotFirst")) robotFirst = call.argument<Any>("robotFirst") as Boolean
        val faqTemplateId = call.argument<Any>("faqTemplateId") as Int
        val vipLevel = call.argument<Any>("vipLevel") as Int
        val showQuitQueue = false
        if (call.hasArgument("showQuitQueue")) call.argument<Any>("showQuitQueue")
        val showCloseSessionEntry = false
        if (call.hasArgument("showCloseSessionEntry")) call.argument<Any>("showCloseSessionEntry")

        // 启动聊天界面
        val source = ConsultSource(sourceUrl, sourceTitle, sourceCustomInfo)
        source.productDetail = productDetail
        source.groupId = groupId.toLong()
        source.staffId = staffId.toLong()
        source.robotId = robotId.toLong()
        source.robotFirst = robotFirst
        source.faqGroupId = faqTemplateId.toLong()
        source.vipLevel = vipLevel
        source.sessionLifeCycleOptions = SessionLifeCycleOptions()
        source.sessionLifeCycleOptions.setCanQuitQueue(showQuitQueue)
        source.sessionLifeCycleOptions.setCanCloseSession(showCloseSessionEntry)
        Unicorn.openServiceActivity(context, sessionTitle, source)
    }

    private fun setCustomUIConfig(call: MethodCall) {
        // 会话窗口上方提示条中的文本字体颜色
        val sessionTipTextColor = call.argument<String>("sessionTipTextColor")
        // 会话窗口上方提示条中的文本字体大小
        val sessionTipTextFontSize = call.argument<Int>("sessionTipTextFontSize")!!
        // 访客文本消息字体颜色
        val customMessageTextColor = call.argument<String>("customMessageTextColor")
        // 客服文本消息字体颜色
        val serviceMessageTextColor = call.argument<String>("serviceMessageTextColor")
        // 消息文本消息字体大小
        val messageTextFontSize = call.argument<Int>("messageTextFontSize")!!
        // 提示文本消息字体颜色
        val tipMessageTextColor = call.argument<String>("tipMessageTextColor")
        // 提示文本消息字体大小
        val tipMessageTextFontSize = call.argument<Int>("tipMessageTextFontSize")!!
        // 输入框文本消息字体颜色
        val inputTextColor = call.argument<String>("inputTextColor")
        // 输入框文本消息字体大小
        val inputTextFontSize = call.argument<Int>("inputTextFontSize")!!
        // 客服聊天窗口背景图片
        val sessionBackgroundImage = call.argument<String>("sessionBackgroundImage")
        // 会话窗口上方提示条中的背景颜色
        val sessionTipBackgroundColor = call.argument<String>("sessionTipBackgroundColor")
        // 访客头像
        val customerHeadImage = call.argument<String>("customerHeadImage")
        // 客服头像
        val serviceHeadImage = call.argument<String>("serviceHeadImage")
        // 消息竖直方向间距
        val sessionMessageSpacing = call.argument<Any>("sessionMessageSpacing") as Float
        // 是否显示头像
        var showHeadImage = true
        if (call.hasArgument("showHeadImage")) showHeadImage = call.argument<Boolean>("showHeadImage")!!
        // 显示发送语音入口，设置为false，可以修改为隐藏
        var showAudioEntry = true
        if (call.hasArgument("showAudioEntry")) showAudioEntry = call.argument<Boolean>("showAudioEntry")!!
        // 显示发送表情入口，设置为false，可以修改为隐藏
        val showEmoticonEntry = true
        if (call.hasArgument("showEmoticonEntry")) call.argument<Any>("showEmoticonEntry")
        // 进入聊天界面，是文本输入模式的话，会弹出键盘，设置为false，可以修改为不弹出
        val autoShowKeyboard = true
        if (call.hasArgument("autoShowKeyboard ")) call.argument<Any>("autoShowKeyboard ")
        var uiCustomization = ysfOptions!!.uiCustomization
        if (uiCustomization == null) {
            ysfOptions!!.uiCustomization = UICustomization()
            uiCustomization = ysfOptions!!.uiCustomization
        }
        uiCustomization!!.topTipBarTextColor = QiYuUtils.parseColor(sessionTipTextColor)
        uiCustomization.topTipBarTextSize = sessionTipTextFontSize.toFloat()
        uiCustomization.textMsgColorRight = QiYuUtils.parseColor(customMessageTextColor)
        uiCustomization.textMsgColorLeft = QiYuUtils.parseColor(serviceMessageTextColor)
        uiCustomization.textMsgSize = messageTextFontSize.toFloat()
        uiCustomization.tipsTextColor = QiYuUtils.parseColor(tipMessageTextColor)
        uiCustomization.tipsTextSize = tipMessageTextFontSize.toFloat()
        uiCustomization.inputTextColor = QiYuUtils.parseColor(inputTextColor)
        uiCustomization.inputTextSize = inputTextFontSize.toFloat()
        uiCustomization.msgBackgroundUri = QiYuUtils.getImageUri(context, sessionBackgroundImage)
        uiCustomization.topTipBarBackgroundColor = QiYuUtils.parseColor(sessionTipBackgroundColor)
        uiCustomization.rightAvatar = QiYuUtils.getImageUri(context, customerHeadImage)
        uiCustomization.leftAvatar = QiYuUtils.getImageUri(context, serviceHeadImage)
        uiCustomization.msgListViewDividerHeight = sessionMessageSpacing.toInt()
        uiCustomization.hideLeftAvatar = !showHeadImage
        uiCustomization.hideRightAvatar = !showHeadImage
        uiCustomization.hideAudio = !showAudioEntry
        uiCustomization.hideEmoji = !showEmoticonEntry
        uiCustomization.hideKeyboardOnEnterConsult = !autoShowKeyboard
    }

    private fun getUnreadCount(call: MethodCall, result: Result) {
        val count = Unicorn.getUnreadCount()
        result.success(count)
    }

    private fun setUserInfo(call: MethodCall) {
        val userId = call.argument<String>("userId")
        val data = call.argument<String>("data")
        val userInfo = YSFUserInfo()
        userInfo.userId = userId
        userInfo.data = data
        Unicorn.setUserInfo(userInfo);
    }

    private fun logout() {
        Unicorn.setUserInfo(null)
    }

    private fun cleanCache() {
        Unicorn.clearCache()
    }

    private fun setupChannel(messenger: BinaryMessenger, context: Context) {
        this.context = context
        channel = MethodChannel(messenger, CHANNEL_NAME)
        channel!!.setMethodCallHandler(this)
    }

    private fun teardownChannel() {
        channel!!.setMethodCallHandler(null)
        channel = null
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}
