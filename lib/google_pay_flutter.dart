import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class GooglePayFlutter {
  static MethodChannel _channel = MethodChannel('google_pay_flutter');

  static Future<bool> get possiblyShowGooglePayButton async {
    final bool possiblyShowGooglePayButton = await _channel.invokeMethod('possiblyShowGooglePayButton');
    return possiblyShowGooglePayButton;
  }

  static Future<Map<bool, String>> requestPayment({
    @required int price,
    @required String currencyCode,
    @required String gateway,
    @required String publicId,
    bool isTestEnvironment = false,
  }) async {
    try {
      var params = <String, dynamic>{
        'price': price,
        'currencyCode': currencyCode,
        'gateway': gateway,
        'publicId': publicId,
        'isTestEnvironment': isTestEnvironment,
      };
      String token = await _channel.invokeMethod('requestPayment', params);
      print('Token: $token');
      return {true: token};
    } on PlatformException catch (ex) {
      print('');
      return {false: '$ex'};
    }
  }
}
