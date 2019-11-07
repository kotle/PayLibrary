package com.pay.library

/**
 * 支付监听
 */
interface PayListener {
    fun success(payHelper: IPay?) {}
    fun faile(payHelper: IPay?) {}
    fun cancel(payHelper: IPay?) {}
}

/**
 * 支付接口
 */
interface IPay {
    fun startPay(listener: PayListener)
}

/**
 * 支付委托
 */
class PayHelper(private val basePay: BasePay) : IPay by basePay

/**
 * 支付基类
 */
abstract class BasePay : IPay {
}