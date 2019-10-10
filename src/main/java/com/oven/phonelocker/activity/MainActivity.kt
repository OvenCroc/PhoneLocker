package com.oven.phonelocker.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.oven.phonelocker.R
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.service.MyService
import com.oven.phonelocker.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity(), View.OnClickListener {
    /**
     * 跳转系统悬浮窗权限界面请求码
     */
    private val FLAT_REQUEST_CODE: Int = 6666

    /**
     * 跳转系统辅助功能设置界面请求码
     */
    private val ACCESSIBILITY_REQUEST_CODE: Int = 8888

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun initView() {
        add_app_tv.setOnClickListener(this)
        manager_app_tv.setOnClickListener(this)
        just_lock_phone_tv.setOnClickListener(this)
        main_tools_bar.title = resources.getString(R.string.app_name)
        setSupportActionBar(main_tools_bar)
    }

    override fun initData() {
        getPermission()
    }

    /**
     * 获取悬浮窗和辅助功能权限
     *
     * @author zhoupan
     * Created at 2019/4/4 9:43
     */
    private fun getPermission() {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermissionUtils.isCanDrawOnOtherApp(this)
            } else {//如果版本默认比M小的话,悬浮窗权限好像默认是开启的,所以这里直接返回true
                true
            }
        ) {
            //判断是否已经开启了辅助功能权限
            if (PermissionUtils.isAccessibilityServiceEnable(this)) {
                startHelperService()
            } else {
                //跳转开启辅助权限界面
                toast("来！请打开辅助功能权限")
                gotoOpenAccessibility()
            }
        } else {
            //跳转开启悬浮窗权限界面
            toast("来！请打开悬浮窗权限")
            gotoOpenDrawOnOtherAppPermission()
        }
    }

    /**
     * 开起服务
     *
     * @author zhoupan
     * Created at 2019/9/30 9:51
     */
    private fun startHelperService() {
        val intent = Intent(this, MyService::class.java)
        startService(intent)
    }

    /**
     * 跳转到系统设置页面,开启悬浮窗权限
     *
     * @author zhoupan
     * Created at 2019/4/4 9:56
     */
    private fun gotoOpenDrawOnOtherAppPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        startActivityForResult(intent, FLAT_REQUEST_CODE)
    }

    /**
     * 跳转开启辅助功能权限系统页面
     *
     * @author zhoupan
     * Created at 2019/4/4 11:15
     */
    private fun gotoOpenAccessibility() {
        val accessibleIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(accessibleIntent, ACCESSIBILITY_REQUEST_CODE)
    }

    override fun onClick(v: View?) {
        when (v) {
            add_app_tv -> {
                val intent = Intent(this, AppListActivity::class.java)
                startActivity(intent)
            }
            just_lock_phone_tv -> {
                val intent = Intent(this, TimePickActivity::class.java)
                startActivity(intent)
            }
            manager_app_tv -> {
                val intent = Intent(this, ManagerAppListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        EventBus.getDefault().post(AppCons.EB_TO_HOME)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FLAT_REQUEST_CODE, ACCESSIBILITY_REQUEST_CODE -> {//从设置悬浮窗的界面回来了 , 从系统设置辅助功能页面回来了
                if (resultCode == RESULT_OK) {
                    toast("悬浮窗权限设置成功")
                }
                getPermission()
            }
        }
    }
}

fun Context.toast(str: String, isPrintLog: Boolean? = false) {
    str.apply {
        Toast.makeText(this@toast, str, Toast.LENGTH_LONG)
            .show()
    }
    if (isPrintLog!!) {
        mylog(str)
    }
}

fun mylog(str: String) {
    if (AppCons.IS_DEBUG) {
        Log.i("zp", str)
    }
}
