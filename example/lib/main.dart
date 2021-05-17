import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_qy_plugin/flutter_qiyu.dart';

// import 'flutter_qiyu.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  QiYu.registerApp(
    appKey: 'f79970e85bcd857128da6c8390d51b9e',
    appName: '职业树',
  );

  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const _platform = const MethodChannel('toFlutterChannelName');

  @override
  void initState() {
    super.initState();
    _platform.setMethodCallHandler(flutterMethod);
  }

  Future<dynamic> flutterMethod(MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'fluMethod':
        print('原生Android调用了flutterMethod方法！！！');
        print('原生Android传递给flutter的参数是：：' + methodCall.arguments);
        startQiYu();
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              TextButton(
                child: Text('联系客服'),
                onPressed: () {
                  startQiYu();
                },
              ),
              TextButton(
                child: Text('版本'),
                onPressed: () {
                  QiYu.getVersion()
                      .then((value) => {print("version = " + value)});
                },
              )
            ],
          ),
        ),
      ),
    );
  }

  void startQiYu() {
    QYUserInfoParams userInfoParams = QYUserInfoParams.fromJson({
      'userId': '111',
      'data':
          '[{\"key\":\"real_name\", \"value\":\"伍明鹏\"},{\"key\":\"mobile_phone\", \"value\":\"${19983550421}\"},{\"index\":0, \"key\":\"vip\", \"label\":\"会员\", \"value\":\"${1}\"}]'
    });
    QiYu.setUserInfo(userInfoParams);

   QYServiceWindowParams serviceWindowParams = QYServiceWindowParams.fromJson({
      'source': {
        'sourceTitle': '职业树(安卓)',
        'sourceUrl': 'https://www.baidu.com',
        'sourceCustomInfo': '我是来自自定义的信息'
      },
      'commodityInfo': {
        'commodityInfoTitle': 'aaa',
        'commodityInfoDesc': 'productId',
        'pictureUrl': 'productImg',
        'commodityInfoUrl': 'http://ssck.hrclass.com.cn/course/914.html',
        'note': '￥${100}',
        'show': true,
        // 'sendByUser': true,
        // 'alwaysSend': true
      },
      'sessionTitle': '职业树',
      'groupId': 0,
      'staffId': 0,
      'robotId': 0,
      'robotFirst': false,
      'faqTemplateId': 0,
      'vipLevel': 0,
      'showQuitQueue': true,
      'showCloseSessionEntry': true
    });
    QiYu.openServiceWindow(serviceWindowParams);
  }
}
