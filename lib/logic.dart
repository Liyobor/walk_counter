import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';


class Logic extends GetxController{
  StreamSubscription? subscription;

  var step = 0.obs;


  static const platform = MethodChannel('com.liyobor.demo/method');
  static const eventChannel = EventChannel('com.liyobor.demo/events');

  static Stream get getDataStream {
    return eventChannel.receiveBroadcastStream().cast();
  }

  Future<void> resetCount()async{
    try{
      await platform.invokeMethod("reset", null);
    } on PlatformException catch (e){
      if (kDebugMode) {
        print("Failed to resetCount status: '${e.message}'.");
      }
    }
  }

  void addEVentChannel() {
    if (subscription != null) {
      subscription!.cancel();
      subscription = null;
    } else {
      subscription = getDataStream.listen((data) {

        if(data["count"]!=null){
          var count = data["count"] as int;
          step.value = count;
        }

      }, onError: (dynamic error) {
        if (kDebugMode) {
          print('Received error: $error');
        }
      });
    }
  }

}