package com.oven.phonelocker.utils

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/10/9.
 */
object Utils {
    fun timeStrMake(time: Int): String {
        return if (time in 0 until 10) {
            "0$time"
        } else
            time.toString()
    }
}