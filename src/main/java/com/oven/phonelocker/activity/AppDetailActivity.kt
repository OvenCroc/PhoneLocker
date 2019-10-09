package com.oven.phonelocker.activity

import android.os.Bundle
import android.view.View
import com.oven.phonelocker.R
import com.oven.phonelocker.entity.AppinfoEntity
import kotlinx.android.synthetic.main.app_detail_activity_layout.*

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/10/8.
 */
class AppDetailActivity : BaseActivity(), View.OnClickListener {
    var entity: AppinfoEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_detail_activity_layout)
    }

    override fun initView() {
        initToolBar()
    }

    override fun initData() {
    }

    private fun initToolBar() {
        titleBar.title = ""
        setSupportActionBar(titleBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        titleBar.setNavigationOnClickListener { finish() }
    }

    override fun onClick(v: View?) {
        when (v) {
        }
    }

}