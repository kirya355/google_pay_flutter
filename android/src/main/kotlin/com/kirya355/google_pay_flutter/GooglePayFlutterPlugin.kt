package com.kirya355.google_pay_flutter

import android.R.attr
import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.*
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*


public class GooglePayFlutterPlugin: FlutterPlugin, MethodCallHandler, ActivityAware,ActivityResultListener {
  private lateinit var channel : MethodChannel
  private lateinit var activity : Activity
  private lateinit var paymentsClient: PaymentsClient
  private var lastResult: Result? = null
  private val LOAD_PAYMENT_DATA_REQUEST_CODE = 355


  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    this.lastResult=result
    if(call.method =="possiblyShowGooglePayButton"){
      possiblyShowGooglePayButton(result)
    }
    else if(call.method =="requestPayment"){
      requestPayment(call.argument("price")!!,call.argument("currencyCode")!!,call.argument("gateway")!!,call.argument("publicId")!!,call.argument("isTestEnvironment")!! )
    }
    else {
      result.notImplemented()
    }
  }
  private fun createPaymentsClient(activity: Activity,isTest:Boolean): PaymentsClient {
    val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(if(isTest)WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION)
            .build()
    return Wallet.getPaymentsClient(activity, walletOptions)
  }
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?):Boolean {
    when (requestCode) {
      // Value passed in AutoResolveHelper
      LOAD_PAYMENT_DATA_REQUEST_CODE -> {
        when (resultCode) {
          Activity.RESULT_OK ->    if (data != null) {
            val paymentData = PaymentData.getFromIntent(data)
            if (paymentData != null) {
              val paymentDataString  = paymentData?.getPaymentMethodToken()?.getToken()
              this.lastResult?.success(paymentDataString)
            }else this.lastResult?.error("PAYMENT_DATA_IS_NULL", " Payment Data is null", null)
          }
          else  this.lastResult?.error("INTENT_IS_NULL", " Intent is null", null)
          Activity.RESULT_CANCELED -> {
            this.lastResult?.error("RESULT_CANCELED", "User canceled the payment", null)
          }
            AutoResolveHelper.RESULT_ERROR -> {
              AutoResolveHelper.getStatusFromIntent(data)?.let {
                this.lastResult?.error("RESULT_ERROR", "AutoResolveHelper.RESULT_ERROR", it.statusCode)
              }
            }
        }
        this.lastResult = null
      }
    }
    return false
  }



  private fun possiblyShowGooglePayButton(result: Result) {
    val request: IsReadyToPayRequest = IsReadyToPayRequest.newBuilder()
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .build()
    paymentsClient = createPaymentsClient(activity, true)
    val task = paymentsClient.isReadyToPay(request)
    task.addOnCompleteListener { completedTask ->
      try {
        val bool: Boolean? = completedTask.getResult(ApiException::class.java)
        result.success(bool)
      } catch (exception: ApiException) {
        result.success(false)
      }
    }
  }

  private fun requestPayment(price:Int, currencyCode: String,gateway: String,publicId:String,isTest:Boolean) {
    val request: PaymentDataRequest = createPaymentDataRequest(price,currencyCode,gateway,publicId) ?: return
    if (request != null) {
        paymentsClient = createPaymentsClient(activity, isTest)
      AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(request), activity, LOAD_PAYMENT_DATA_REQUEST_CODE)
    }
  }


  private fun createTokenizationParameters(gateway:String, publicId: String): PaymentMethodTokenizationParameters? {
    return PaymentMethodTokenizationParameters.newBuilder()
            .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
            .addParameter("gateway", gateway)
            .addParameter("gatewayMerchantId", publicId)
            .build()
  }
  private fun createPaymentDataRequest(price: Int, currencyCode: String,gateway:String, publicId: String): PaymentDataRequest? {
    val _tokenizationParameters=createTokenizationParameters(gateway,publicId)
    if (_tokenizationParameters == null) return null
    val request: PaymentDataRequest.Builder = PaymentDataRequest.newBuilder()
            .setTransactionInfo(
                    TransactionInfo.newBuilder()
                            .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                            .setTotalPrice(price.toString())
                            .setCurrencyCode(currencyCode)
                            .build())
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .setCardRequirements(
                    CardRequirements.newBuilder()
                            .addAllowedCardNetworks(Arrays.asList(
                                    WalletConstants.CARD_NETWORK_AMEX,
                                    WalletConstants.CARD_NETWORK_DISCOVER,
                                    WalletConstants.CARD_NETWORK_VISA,
                                    WalletConstants.CARD_NETWORK_MASTERCARD))
                            .build())
    request.setPaymentMethodTokenizationParameters(_tokenizationParameters)
    return request.build()
  }



  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "google_pay_flutter")
    channel.setMethodCallHandler(this)
    channel.invokeMethod("shit", "SSSShit")
  }
  override fun onDetachedFromActivityForConfigChanges(){}
  override fun onReattachedToActivityForConfigChanges(@NonNull binding: ActivityPluginBinding){
    binding.addActivityResultListener(this)
    activity= binding.getActivity()
  }
  override fun onDetachedFromActivity(){}
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    binding.addActivityResultListener(this)
    activity= binding.getActivity()

  }
  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_pay")
      val plugin = GooglePayFlutterPlugin()
      channel.setMethodCallHandler(plugin)
      registrar.addActivityResultListener(plugin)
      plugin.activity = registrar.activity()
      plugin.paymentsClient = plugin.createPaymentsClient(registrar.activity(), true)
    }
  }

}