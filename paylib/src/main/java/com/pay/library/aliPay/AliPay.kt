package com.pay.library.aliPay

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.alipay.sdk.app.PayTask
import com.pay.library.BasePay
import com.pay.library.PayListener
import java.util.HashMap

/**
 * 支付宝支付
 */
class AliPay(val orderInfo: String, val activity: Activity) : BasePay() {
    companion object {
        val aliPayResultStatus = HashMap<String?, String?>().apply {
            put("9000", "操作成功")
            put("4000", "系统异常")
            put("4001", "数据格式不正确")
            put("4003", "该用户绑定的支付宝账户被冻结或不允许支付")
            put("4004", "该用户已解除绑定")
            put("4005", "绑定失败或没有绑定")
            put("4006", "订单支付失败")
            put("4010", "重新绑定账户")
            put("6000", "支付服务正在进行升级操作")
            put("6001", "用户中途取消支付操作")
            put("7001", "网页支付失败")
        }
    }

    private var _payResult: PayResult? = null
    val payResult: PayResult?
        get() = _payResult

    //handler，有一部分代码需要在主线程运行
    private val mainHandler = Handler(Looper.getMainLooper())
    //支付结果监听
    private var payListener: PayListener? = null
    //支付运行线程
    private val payRun = Runnable {
        val aliPay = PayTask(activity)
        val payV2 = aliPay.payV2(orderInfo, true)
        mainHandler.post {
            val result = PayResult(payV2)
            //对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
            _payResult = result
            // 同步返回需要验证的信息
            val resultInfo: String? = result.result
            val resultStatus: String? = result.resultStatus
            // 判断resultStatus 为9000则代表支付成功
            if (TextUtils.equals(resultStatus, "9000")) {
                //成功，该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                payListener?.success(this)
            } else {
                //失败，该笔订单真实的支付结果，需要依赖服务端的异步通知。
                payListener?.faile(this)
            }
        }
    }
    private val payThread = Thread(payRun)

    override fun startPay(listener: PayListener) {
        payListener = listener
        payThread.start()
    }
}