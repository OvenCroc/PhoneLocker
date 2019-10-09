package com.oven.phonelocker.service

import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.oven.phonelocker.activity.KillProcessActivity
import com.oven.phonelocker.activity.mylog
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.entity.AppinfoEntity
import com.oven.phonelocker.receiver.TimeReceiver
import com.oven.phonelocker.utils.BoxHelper
import com.oven.phonelocker.utils.SPUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/27.
 */
class MyService : AccessibilityService() {
    var calendar: Calendar? = null
    override fun onCreate() {
        super.onCreate()
        calendar = Calendar.getInstance(Locale.CHINA)
        EventBus.getDefault().register(this)
    }

    private var datalist: MutableList<AppinfoEntity> = mutableListOf()
    override fun onInterrupt() {
        mylog("服务暂停")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.apply {
            datalist.forEach {
                if (calendar?.get(Calendar.HOUR_OF_DAY)!! in 18..22) {//todo 临时处理 17-23之间是可以耍的
                    return
                }
                if (it.packageName.toString() == event.packageName.toString()) {//如果打开了受控制的app,就跳转到一个页面
                    if (System.currentTimeMillis() - it.limitTime > AppCons.USE_TIME_LIMIT) {            //判断是否已经通过自己的supercode了
                        val i = Intent(this@MyService, KillProcessActivity::class.java)
                        i.putExtra("targetPackageName", event.packageName.toString())
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                        return@forEach
                    } else {
                        //提示用户当前还有多少分钟可以耍
                        val leaveTime =
                            (System.currentTimeMillis() - it.limitTime) / 1000 / 60
                        mylog("你还有少于 ${leaveTime + 1} 分钟可以耍")
                    }
                }
            }

        }
    }

    @Subscribe
    fun onEvent(message: String) {
        when (message) {
            AppCons.EB_DB_UPDATE -> {//表示数据更新了
                getDBData()//重新获取一下数据库里面的数据
            }
        }
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        mylog("服务启动")
        getDBData()
        //创建一个计时器
        createAlarms()
    }

    /**
     * 获取数据库里面的受控制的app的数据
     *
     * @author zhoupan
     * Created at 2019/10/8 14:54
     */
    private fun getDBData() {
        datalist = (BoxHelper.getList(AppinfoEntity::class.java)
            ?: mutableListOf()) as MutableList<AppinfoEntity>
    }

    /**
     * 创建一个计时器
     *
     * @author zhoupan
     * Created at 2019/10/8 10:36
     */
    private fun createAlarms() {
        val modelStartTime = SPUtils.getInstance().getString(AppCons.sp_start_time)
        val modelEndTime = SPUtils.getInstance().getString(AppCons.sp_end_time)
        if (!modelStartTime.isNullOrBlank()) {//本地已经选择过开始时间和结束时间了,设置开始时间的
            createAlarm(modelStartTime, AppCons.MODEL_START)
            mylog("设置模式开始时候的闹铃触发$modelStartTime")
        }
        if (!modelEndTime.isNullOrBlank()) {
            createAlarm(modelEndTime, AppCons.MODEL_END)
            mylog("设置模式结束时候的闹铃触发$modelEndTime")
        }
    }

    /**
     * 根据时间创建闹铃
     *  @param time 传入的格式是00:00
     * @author zhoupan
     * Created at 2019/10/8 11:01
     */
    private fun createAlarm(time: String, action: String) {
        val calendar = Calendar.getInstance(Locale.CHINA)
        calendar.set(Calendar.HOUR_OF_DAY, time.split(":")[0].toInt())
        calendar.set(Calendar.MINUTE, time.split(":")[1].toInt())
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TimeReceiver::class.java)
        intent.action = action
        val pi = PendingIntent.getBroadcast(this, 0, intent, 0)
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
    }
}