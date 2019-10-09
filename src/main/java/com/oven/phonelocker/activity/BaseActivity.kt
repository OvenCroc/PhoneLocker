package com.oven.phonelocker.activity

import androidx.appcompat.app.AppCompatActivity
import com.noober.background.BackgroundLibrary

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/29.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        BackgroundLibrary.inject(this)
        initView()
        initData()
    }

    abstract fun initView()
    abstract fun initData()

    override fun finish() {
        mylog("${this.javaClass.name.toString()} call finish")
        super.finish()
    }
}