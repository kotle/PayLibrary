package com.pay.library.wxpay

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.pay.library.WxUtils

/**
 * 微信分享的回调activity
 */
class WXEntryActivity:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        WxUtils.handleWxIntent(this,intent)
        title = "微信分享"
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        WxUtils.handleWxIntent(this,intent)
    }
}