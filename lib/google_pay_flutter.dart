import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
/*Copyright (c) KIRILL APARIN

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/
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
