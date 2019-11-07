package com.pay.library.wxpay

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * 微信支付回调的activity
 */
class WXPayEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        WeiXinPay.instance?.wxApi?.handleIntent(intent, WeiXinPay.instance)
        title = "微信支付"
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        WeiXinPay.instance?.wxApi?.handleIntent(intent, WeiXinPay.instance)
        finish()
    }

    override fun onDestroy() {
        WeiXinPay.instance = null
        super.onDestroy()
    }
}