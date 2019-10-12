package com.oven.phonelocker.utils

import com.oven.phonelocker.App

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

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    fun dp2px(dpValue: Float): Int {
        val scale = App.app?.resources?.displayMetrics?.density ?: 0f
        return (dpValue * scale + 0.5f).toInt()
    }
}