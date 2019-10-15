package com.oven.phonelocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.oven.phonelocker.activity.mylog

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/10/15.
 */
class ScreenBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {//解锁
                mylog("SCREEN_ON_ACTION ")
            }
            Intent.ACTION_SCREEN_OFF -> {//锁屏
                mylog("ACTION_SCREEN_OFF ")
                //锁屏的时候创建一个activity
            }
            Intent.ACTION_USER_PRESENT -> {//开屏
                mylog("SCREEN_PRESENT_ACTION ")
            }
        }
    }
}