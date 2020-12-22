# google_pay_flutter

Google Pay for Flutter. Currently works with CloudPayments

## Getting Started

The package is very simple.



for check possibly Show Google Pay Button use
```
GooglePayFlutter.possiblyShowGooglePayButton
```
For call Google Pay menu

```
Map<bool, String> map = await GooglePayFlutter.requestPayment(
      currencyCode: 'RUB',
      gateway: 'yourgateway',
      price: 355,
      publicId: 'yourgatewayid',
      isTestEnvironment: true //optional, default is false
    );
```
if map return false:'some message', operation is failed, else all is fine
