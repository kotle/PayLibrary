package com.paylibrary.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pay.library.IPay
import com.pay.library.PayHelper
import com.pay.library.PayListener
import com.pay.library.aliPay.AliPay
import com.pay.library.wxpay.WeiXinPay
import com.tencent.mm.opensdk.modelpay.PayReq
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val payListener = object : PayListener {
        override fun success(payHelper: IPay?) {
            super.success(payHelper)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aliPay.setOnClickListener {
            PayHelper(AliPay("", this)).startPay(payListener)
        }
        wxPay.setOnClickListener {
            val request = PayReq()
            request.appId = "wxd930ea5d5a258f4f"
            request.partnerId = "1900000109"
            request.prepayId = "1101000000140415649af9fc314aa427"
            request.packageValue = "Sign=WXPay"
            request.nonceStr = "1101000000140429eb40476f8896f4c9"
            request.timeStamp = "1398746574"
            request.sign = "7FFECB600D7157C5AA49810D2D8F28BC2811827B"
            PayHelper(WeiXinPay(this, request)).startPay(payListener)
        }
    }
}
