package com.pay.library.wxpay

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * 微信支付回调的activity
 */
class WXPayEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    override fun onResp(baseResp: BaseResp?) {
        WeiXinPay.instance?.onResp(baseResp)
    }

    override fun onReq(p0: BaseReq?) {
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        window.attributes?.alpha = 0f
        WeiXinPay.instance?.wxApi?.handleIntent(intent, this)
        title = "微信支付"
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        WeiXinPay.instance?.wxApi?.handleIntent(intent, this)
    }

    override fun onDestroy() {
        WeiXinPay.instance = null
        super.onDestroy()
    }
}