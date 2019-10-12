package com.oven.phonelocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.oven.phonelocker.activity.mylog
import com.oven.phonelocker.activity.toast
import com.oven.phonelocker.common.AppCons

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/10/8.
 */
class TimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.apply {
            intent?.apply {
                when (action) {
                    AppCons.MODEL_START -> {
                        mylog("作死模式开始了")
                    }
                    AppCons.MODEL_END -> {
                        mylog("作死模式结束了,可以耍手机了")
                    }
                    AppCons.MODEL_AUTO_LOGIN -> {
                        toast("autologin", true)
                    }
                }
            }
        }
    }
}