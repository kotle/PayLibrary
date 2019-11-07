package com.pay.library.wxpay

import android.annotation.SuppressLint
import android.content.Context
import com.pay.library.BasePay
import com.pay.library.PayListener
import com.pay.library.R
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * 微信支付，🌰
request.appId = "wxd930ea5d5a258f4f"
request.partnerId = "1900000109"
request.prepayId = "1101000000140415649af9fc314aa427"
request.packageValue = "Sign=WXPay"
request.nonceStr = "1101000000140429eb40476f8896f4c9"
request.timeStamp = "1398746574"
request.sign = "7FFECB600D7157C5AA49810D2D8F28BC2811827B"
 */
class WeiXinPay(val context: Context, val request: PayReq) : BasePay() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        internal var instance: WeiXinPay? = null
    }

    private val appSecret = context.getString(R.string.wx_app_secret)
    private var wxPayListener: PayListener? = null
    private val appId = context.getString(R.string.wx_app_id)
    internal val wxApi = WXAPIFactory.createWXAPI(context.applicationContext, appId, true)

    init {
        instance = this
    }


    override fun startPay(listener: PayListener) {
        wxPayListener = listener
        wxApi.sendReq(request)
    }

    private var _baseResp: BaseResp? = null
    val baseResp: BaseResp?
        get() = _baseResp
    val errorMessage: String?
        get() = _baseResp?.errStr
    val errorCode: Int?
        get() = _baseResp?.errCode

    internal fun onResp(baseResp: BaseResp?) {
        when (baseResp?.errCode) {
            BaseResp.ErrCode.ERR_OK -> {//成功
                wxPayListener?.success(this)
            }
            BaseResp.ErrCode.ERR_COMM -> {//错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                wxPayListener?.faile(this)
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {//取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
                wxPayListener?.cancel(this)
            }
        }
        instance = null
    }
}