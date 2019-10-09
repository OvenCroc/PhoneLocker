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

    /**
     * 获取所有的数据
     *
     * @author zhoupan
     * Created at 2019/10/9 14:02
     */
    fun getList(cls: Class<*>): MutableList<out Any>? {
        return boxStore?.boxFor(cls)?.query()?.build()?.find()
    }
}