package com.oven.phonelocker.activity

import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.RequiresApi
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.entity.AppinfoEntity
import com.oven.phonelocker.entity.AppinfoEntity_
import com.oven.phonelocker.utils.BoxHelper
import kotlinx.android.synthetic.main.kill_process_activity_layout.*
import org.greenrobot.eventbus.EventBus
import java.util.*


/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/29.
 */
class KillProcessActivity : BaseActivity(), View.OnClickListener {
    /**
     * 需要被杀的app的包名
     */
    private var targetPackageName = ""
    var entity: MutableList<AppinfoEntity> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.oven.phonelocker.R.layout.kill_process_activity_layout)
    }

    override fun initView() {
        kill_bg_process_btn.setOnClickListener(this)
    }

    override fun initData() {
        targetPackageName = intent.getStringExtra("targetPackageName") ?: ""
        entity = BoxHelper.boxStore?.boxFor(AppinfoEntity::class.java)?.query()
            ?.equal(AppinfoEntity_.packageName, targetPackageName)?.build()?.find()
            ?: mutableListOf()
        kill_bg_process_btn.text = "KILL ${entity[0].appName}"
        super_code_tv.text = generateSuperCode()
    }

    /**
     * 随机生成一串字符串，真的就是随机生成。。。
     *
     * @author zhoupan
     * Created at 2019/10/8 14:29
     */
    private fun generateSuperCode(): String {
        val str =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*-()_+{}:,."
        val random = Random()
        val sb = StringBuffer()
        for (i in 0 until 32) {
            val number = random.nextInt(80)
            sb.append(str[number])
        }
        return sb.toString()
    }

    override fun onClick(v: View?) {
        when (v) {
            kill_bg_process_btn -> {

                if (super_code_tv.text.toString() == super_code_et.text.toString()
//                    || super_code_et.text.toString() == "1111"
                ) {
                    toast("小伙子不错哟，都输对了哈")
                    //更新数据库里面的使用时间
                    entity?.get(0)?.limitTime = System.currentTimeMillis()
                    BoxHelper.boxStore?.boxFor(AppinfoEntity::class.java)?.put(entity?.get(0))
//                    EventBus.getDefault().post(AppCons.EB_DB_UPDATE)
                    Handler().postDelayed({
                        finish()
                    }, 1000)
                } else {
                    val str = "输错了，耍锤子耍，滚切学习"
                    toast(str)
                    changeFlyViewText(str)
                    super_code_tv.text = generateSuperCode()//重新刷新code
                }

//                if (targetPackageName.isNullOrEmpty().not()) {//需要终止的包名不为空的时候
//                    killProcess()//杀掉app
//                }
            }
        }
    }

    private fun changeFlyViewText(str: String, isNeedClearText: Boolean? = true) {
        if (isNeedClearText!!) {
            Handler().postDelayed({
                EventBus.getDefault().post(AppCons.EB_MODIFY_FLY_VIEW_TEXT + "|")
            }, 3000)
        }
        EventBus.getDefault().post(AppCons.EB_MODIFY_FLY_VIEW_TEXT + "|$str")
    }

    /**
     * 根据包名杀进程
     *
     * @author zhoupan
     * Created at 2019/9/29 9:59
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun killProcess() {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.killBackgroundProcesses(targetPackageName)
        mylog("has processed kill")
    }


}