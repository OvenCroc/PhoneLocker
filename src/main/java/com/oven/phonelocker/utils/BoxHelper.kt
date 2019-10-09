package com.oven.phonelocker.utils

import android.content.Context
import com.oven.phonelocker.entity.MyObjectBox
import io.objectbox.BoxStore

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/30.
 */
object BoxHelper {
    var boxStore: BoxStore? = null
    fun init(context: Context) {
        boxStore = MyObjectBox.builder().androidContext(context).build()
    }

}