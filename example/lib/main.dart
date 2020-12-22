import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:google_pay_flutter/google_pay_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const String _publicId = 'Your publicId';
  static const String _gateway = 'Your gateway';

  bool _possiblyShowGooglePayButton = false;
  bool _loading = false;
  String _googlePayToken = 'Unknown';

  @override
  void initState() {
    super.initState();
    possiblyShowGooglePayButton();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('GooglePayFlutterPlugin'),
        ),
        body: Stack(
          children: [
            Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text('possiblyShowGooglePayButton: $_possiblyShowGooglePayButton\n\nToken: $_googlePayToken'),
                  if (_possiblyShowGooglePayButton)
                    FlatButton(
                      child: Text('Pay'),
                      onPressed: payButton,
                      color: Colors.blue,
                    )
                ],
              ),
            ),
            if (_loading)
              Container(
                height: double.infinity,
                width: double.infinity,
                color: Colors.black.withOpacity(0.3),
                child: Center(
                  child: CircularProgressIndicator(),
                ),
              )
          ],
        ),
      ),
    );
  }

  Future<void> possiblyShowGooglePayButton() async {
    bool possiblyShowGooglePayButton;
    try {
      possiblyShowGooglePayButton = await GooglePayFlutter.possiblyShowGooglePayButton;
    } on PlatformException {
      possiblyShowGooglePayButton = false;
    }
    setState(() {
      _possiblyShowGooglePayButton = possiblyShowGooglePayButton;
    });
  }

  void payButton() async {
    setState(() {
      _loading = true;
    });
    Map<bool, String> x = await GooglePayFlutter.requestPayment(
      currencyCode: 'RUB',
      gateway: _gateway,
      price: 10,
      publicId: _publicId,
      isTestEnvironment: true,
    );
    _googlePayToken = x.values.first;
    setState(() {
      _loading = false;
    });
  }
}
