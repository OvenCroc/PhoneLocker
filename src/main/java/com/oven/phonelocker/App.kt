package com.oven.phonelocker

import android.app.Application
import com.oven.phonelocker.utils.BoxHelper

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/30.
 */
class App : Application() {
    companion object {
        var app: Application? = null
    }


    override fun onCreate() {
        super.onCreate()
        app = this
        BoxHelper.init(this)
    }
}