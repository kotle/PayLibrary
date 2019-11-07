使用说明
===
微信支付
=====
1.在*gradle.properties*中，配置账号。
```yml
#包名/applicationId(微信支付回调需要)
packageName=com.paylibrary.demo
#微信支付的AppId,可以不需要
wxAppId=null
#微信登录分享的secret，可以不需要
wxAppSecret=null
```
2.启动微信支付
```kotlin
 PayHelper(WeiXinPay(Activity,PayReq)).startPay( payListener)
```

支付宝支付
=====
1.在根目录的*build.gradle*中设置aar文件目录
```gradle
allprojects {
    repositories {
        // 添加下面的内容
        flatDir {
            dirs 'libs'
        }

        google()
        jcenter()
    }
}
```
2.复制paylib模块lib里面的支付宝aar文件到app模块的lib目录下
3.启动支付宝支付
```kotlin
 PayHelper(AliPay(OrderInfo,Activity)).startPay( payListener)
```

微信分享和微信登录
=====
提供一个工具类WxUtils来实现微信登录和微信分享

1.微信登录
```kotlin
fun login(context: Context, call: WxLoginCall?): Boolean
```
2.微信分享
```kotlin
fun share(context: Context, title: String,desc: String,url: String,type: Int,bitmap: Bitmap )
```