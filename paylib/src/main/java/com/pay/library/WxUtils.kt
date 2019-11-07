package com.pay.library

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * 微信分享，登录工具类
 */
interface WxLoginCall {
    fun onSuccess(wxUserInfo: WxUserInfo)
    fun onFail(errorMessage: String)
}

data class WxUserInfo(
    var openid: String?,
    var nickname: String?,
    var sex: Int?,
    var language: String?,
    var city: String?,
    var province: String?,
    var country: String?,
    var headimgurl: String?,
    var privilege: ArrayList<*>?,
    var unionid: String?,
    var accessToken: String?
)

object WxUtils : IWXAPIEventHandler {
    private const val SHARE_TYPE_SESSION = SendMessageToWX.Req.WXSceneSession

    private const val SHARE_TYPE_TIMELINE = SendMessageToWX.Req.WXSceneTimeline

    // 用于区分微信的操作:登录\分享等
    private const val TRANSACTION_TYPE_SHARE = "WECHAT_SHARE"

    private const val TRANSACTION_TYPE_LOGIN = "WECHAT_LOGIN"

    // 分享
    const val TYPE_SHARE = 0

    // 登录
    const val TYPE_LOGIN = 1

    //未知类型
    const val TYPE_UNKNOWN = 2


    private const val saveWxRefreshToken = "saveWxRefreshToken"
    /**
     * 微信支付
     */
    private var wxApi: IWXAPI? = null
    //登录回调
    private var wxLoginListener: WxLoginCall? = null
    private lateinit var appSecret: String
    private lateinit var appId: String
    /**
     * 初始化
     */
    private fun getWxApi(context: Context): IWXAPI {
        if (wxApi == null) {
            appId = context.getString(R.string.wx_app_id)
            appSecret = context.getString(R.string.wx_app_secret)
            //创建微信api并注册到微信
            wxApi = WXAPIFactory.createWXAPI(context, appId, true)
            wxApi?.registerApp(appId)
        }
        return wxApi!!
    }

    override fun onResp(baseResp: BaseResp?) {
        when (baseResp?.type) {
            ConstantsAPI.COMMAND_PAY_BY_WX -> {
                //微信支付
            }
            else -> {
                //微信登录分享等操作
                when (getType(baseResp)) {
                    TYPE_SHARE -> {

                    }
                    TYPE_LOGIN -> {
                        onRespByLogin(baseResp, wxLoginListener)
                    }
                }
            }
        }
    }


    override fun onReq(baseReq: BaseReq?) {
    }


    /**
     * 微信登录
     */
    fun login(context: Context, call: WxLoginCall?): Boolean {
        wxLoginListener = call
        //发起登录请求
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "wx_login"
        req.transaction = buildTransaction(TRANSACTION_TYPE_LOGIN)
        return getWxApi(context).sendReq(req)
    }

    //用户登录的回调
    private fun onRespByLogin(baseResp: BaseResp?, call: WxLoginCall?) {
        baseResp ?: return
        when (baseResp.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                //成功
                getUserInfo(baseResp, call)
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                //取消
                call?.onFail("用户取消登录")
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                //拒绝
                call?.onFail("用户拒绝登陆")
            }
            else -> {
                call?.onFail("其他错误：${baseResp.errCode}")
            }
        }
    }

    //获取用户信息
    private fun getUserInfo(baseResp: BaseResp, call: WxLoginCall?) {
        thread {
            val tokenResult =
                httpGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid=$appId&secret=$appSecret&code=${(baseResp as SendAuth.Resp).code}&grant_type=authorization_code")
            if (tokenResult == null) {
                call?.onFail("请求用户信息失败")
            } else {
                val json = JSONObject(tokenResult)
                val accessToken = json["access_token"] as? String
                val refreshToken = json["refresh_token"] as? String
                val openid = json["openid"] as? String
                val scope = json["scope"] as? String
                val unionid = json["unionid"] as? String
                val expiresIn = json["expires_in"] as? Int
                val userInfo =
                    httpGet("https://api.weixin.qq.com/sns/userinfo?access_token=$accessToken&openid=$openid")
                if (userInfo == null) {
                    call?.onFail("请求用户信息失败")
                } else {
                    val userInfoJson = JSONObject(userInfo)
                    call?.onSuccess(
                        WxUserInfo(
                            openid = userInfoJson["openid"] as? String,
                            nickname = userInfoJson["nickname"] as? String,
                            sex = userInfoJson["sex"] as? Int,
                            language = userInfoJson["language"] as? String,
                            city = userInfoJson["city"] as? String,
                            province = userInfoJson["province"] as? String,
                            country = userInfoJson["country"] as? String,
                            headimgurl = userInfoJson["headimgurl"] as? String,
                            privilege = userInfoJson["privilege"] as? ArrayList<*>,
                            unionid = userInfoJson["unionid"] as? String,
                            accessToken = accessToken
                        )
                    )
                }
            }
        }
    }

    //发送http请求，如果错误返回null，否则返回结果
    private fun httpGet(urlStr: String): String? {
        //获取token
        val url =
            URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10 * 1000
        val code = conn.responseCode
        return if (code == 200) {
            val inputStream = conn.inputStream
            inputStream.bufferedReader().readText()
        } else {
            null
        }
    }

    /**
     * 通过刷新token获取新的accessToken
     * 具体情况再处理
     */
    fun refreshToken(refreshToken: String) {
        val result =
            httpGet("https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=$appId&grant_type=refresh_token&refresh_token=$refreshToken")
        if (result != null) {

        }
    }

    /**
     * 分享到微信
     */
    fun share(
        context: Context,
        title: String,
        desc: String,
        url: String,
        type: Int,
        bitmap: Bitmap
    ) {
        //初始化一个WXWebpageObject，填写url
        val webpage = WXWebpageObject()
        webpage.webpageUrl = url

        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(webpage)
        msg.title = title
        msg.setThumbImage(bitmap)
        msg.description = desc

        //构造一个Req
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction(TRANSACTION_TYPE_SHARE)
        req.message = msg
        req.scene = type
        //调用api接口，发送数据到微信
        getWxApi(context).sendReq(req)
    }

    private fun buildTransaction(type: String?): String? {
        return if (type == null) System.currentTimeMillis().toString() else "$type${System.currentTimeMillis()}"
    }

    private fun getType(resp: BaseResp?): Int {
        resp ?: return TYPE_UNKNOWN
        return when {
            resp.transaction.startsWith(TRANSACTION_TYPE_SHARE) -> TYPE_SHARE
            resp.transaction.startsWith(TRANSACTION_TYPE_LOGIN) -> TYPE_LOGIN
            else -> TYPE_UNKNOWN
        }
    }

    internal fun handleWxIntent(context: Context, intent: Intent?) {
        getWxApi(context).handleIntent(intent, this)
    }

    /*    */
    /**
     * 保存刷新token到本地
     *//*
    fun saveRefreshTokenToSp(refreshToken: String?) {
        refreshToken?.let {
            //保存刷新token，利用刷新token去拿到新tonken
            application.getSharedPreferences("token", Context.MODE_PRIVATE).edit().apply {
                putString(saveWxRefreshToken, refreshToken)
                apply()
            }
        }
    }

    */
    /**
     * 获取刷新token
     *//*
    fun getRefreshTokenFromSp(): String? {
        return application.getSharedPreferences("token", Context.MODE_PRIVATE)
            .getString(saveWxRefreshToken, null)
    }*/
}