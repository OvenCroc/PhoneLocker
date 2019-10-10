package com.oven.phonelocker.service

import android.accessibilityservice.AccessibilityService
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.DecelerateInterpolator
import com.noober.background.BackgroundLibrary
import com.oven.phonelocker.R
import com.oven.phonelocker.activity.KillProcessActivity
import com.oven.phonelocker.activity.mylog
import com.oven.phonelocker.activity.toast
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.entity.AppinfoEntity
import com.oven.phonelocker.receiver.TimeReceiver
import com.oven.phonelocker.utils.BoxHelper
import com.oven.phonelocker.utils.SPUtils
import com.oven.phonelocker.utils.Utils
import kotlinx.android.synthetic.main.add_view_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/27.
 */
class MyService : AccessibilityService(), View.OnClickListener, View.OnTouchListener {

    var calendar: Calendar? = null
    var alertView: View? = null
    var anim: ObjectAnimator? = null
    override fun onCreate() {
        super.onCreate()
        calendar = Calendar.getInstance(Locale.CHINA)
        EventBus.getDefault().register(this)
        BackgroundLibrary.inject(this)
        addViewToWindowManager()
    }

    var windowManager: WindowManager? = null
    private var datalist: MutableList<AppinfoEntity> = mutableListOf()
    override fun onInterrupt() {
        toast("服务暂停", true)
    }

    override fun onDestroy() {
        toast("服务被销毁", true)
        alertView?.apply {
            windowManager?.removeView(this)
        }
        alertView = null
        windowManager = null
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    /**
     * 添加view到windowmananger上
     *
     * @author zhoupan
     * Created at 2019/4/4 15:55
     */
    private fun addViewToWindowManager() {
        if (windowManager == null) {
            windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        }
        alertView = LayoutInflater.from(this).inflate(R.layout.add_view_layout, null)
        alertView?.apply {
            this.alert_view_tv.text = "๑乛◡乛๑"
            this.alert_view_tv.setOnClickListener(this@MyService)
            var layoutParams = initLayoutParam()
            windowManager?.addView(this, layoutParams)
        }
    }


    /**
     * 初始化添加view的layoutparam
     *
     * @author zhoupan
     * Created at 2019/4/4 16:03
     */
    private fun initLayoutParam(): WindowManager.LayoutParams {
        var params: WindowManager.LayoutParams = WindowManager.LayoutParams()
        // compatible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        // set bg transparent
        params.format = PixelFormat.RGBA_8888
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.x = 0
        params.y = 0
        // window size
        params.gravity = Gravity.TOP or Gravity.RIGHT
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        return params
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.apply {
            mylog("eventtype: ${AccessibilityEvent.eventTypeToString(eventType)}")
            autoSign(event)
            doAppController(event)
        }
    }

    /**
     * 自动签到功能
     *
     * @author zhoupan
     * Created at 2019/10/9 16:57
     */
    private fun autoSign(event: AccessibilityEvent) {
        if (event.packageName == "com.ultrapower.android.me.ry") {//签到app
            val info: AccessibilityNodeInfo? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    rootInActiveWindow
                } else {
                    null
                }
            info?.apply {
                //                    tvCancel
                when (event.className) {
                    "com.ultrapower.android.main.MainActivity" -> {//进入首页了之后,点击应用
                        performClick("bottomBar2", info)
                    }
                    "com.ultrapower.oatimer.ui.OATimerActivity" -> {//进入地图页面
                        Handler().postDelayed({
                            performClick("ll_clock_button", info)
                        }, 2000)
                    }
                }
                performClick("item_recycler_app", info)//点击地图签到
                info.recycle()
            }
            mylog("event.classname!!!: ${event.className}")
        }
    }

    /**
     * 根据viewid,执行点击事件
     *
     * @author zhoupan
     * Created at 2019/10/10 14:16
     */
    private fun performClick(viewId: String, info: AccessibilityNodeInfo) {
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            info?.findAccessibilityNodeInfosByViewId("${info.packageName}:id/$viewId")
        } else {
            null
        }
        if (list != null && list.size > 0 && list[0].isClickable) {
            list[0].performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            list[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            mylog("perform $viewId click")
        } else {
            mylog("no view $viewId")
        }
    }

    /**
     * 控制app流程
     *
     * @author zhoupan
     * Created at 2019/10/9 16:56
     */
    private fun doAppController(event: AccessibilityEvent) {
        datalist.forEach {
            if (it.packageName.toString() == event.packageName.toString()) {//如果打开了受控制的app,就跳转到一个页面
                val zoneStrArr = it.allowTimeZone?.split(",")
                val df = SimpleDateFormat("HH:mm")
                val startTime = df.parse(zoneStrArr?.get(0)!!)
                val endTime = df.parse(zoneStrArr?.get(1)!!)
                val nowTime =
                    df.parse(
                        Utils.timeStrMake(
                            calendar?.get(Calendar.HOUR_OF_DAY) ?: 0
                        ) + ":" + Utils.timeStrMake(calendar?.get(Calendar.MINUTE) ?: 0)
                    )
                if (nowTime.time in startTime.time..endTime.time) {
                    mylog("在时间内，耍吧你就")
                    it.limitTime = System.currentTimeMillis()
                    return
                }
                if (System.currentTimeMillis() - it.limitTime > AppCons.USE_TIME_LIMIT) {
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

    @Subscribe
    fun onEvent(message: String) {
        when (message) {
            AppCons.EB_DB_UPDATE -> {//表示数据更新了
                getDBData()//重新获取一下数据库里面的数据
            }
            AppCons.EB_TO_HOME -> {//回收也
                this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            }
        }
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        toast("服务启动", true)
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

    private fun callApp() {
        val intent = Intent(Intent.ACTION_MAIN)
        /**知道要跳转应用的包命与目标Activity*/
        val componentName = ComponentName(
            "com.ultrapower.android.me.ry",
            "com.ultrapower.android.login.SplashActivity"
        )
        intent.component = componentName
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.alert_view_tv -> {
                toast("∑(っ°Д°;)っ\n点我干嘛,快点学习啊")
                startAnim()
            }
        }
    }

    /**
     * 动起来
     *
     * @author zhoupan
     * Created at 2019/10/10 16:57
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun startAnim() {
        alertView?.apply {
            if (anim == null) {
                anim = ObjectAnimator.ofFloat(alertView, "TranslationY", 0f, 200f)
            }
            anim?.apply {
                interpolator = DecelerateInterpolator(1f)
                duration = 1000
                if (this.isRunning) {
                    return
                }
                start()
                Handler().postDelayed({
                    reverse()
                    invalidate()
                }, 4000)
            }
            invalidate()
        }

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.alert_view_tv -> {
                handlerTouchEvent(v, event)
                return true
            }
            else -> {
                return false
            }
        }
    }

    private fun handlerTouchEvent(v: View, event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {//点下的时候
                mylog("点击了下")
            }
            MotionEvent.ACTION_MOVE -> {//移动
                mylog("在移动了")
            }
            MotionEvent.ACTION_UP -> {
                mylog("抬手了...")
            }
        }
    }


}