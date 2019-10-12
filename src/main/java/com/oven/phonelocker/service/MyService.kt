package com.oven.phonelocker.service

import android.accessibilityservice.AccessibilityService
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import com.noober.background.BackgroundLibrary
import com.oven.phonelocker.R
import com.oven.phonelocker.activity.KillProcessActivity
import com.oven.phonelocker.activity.mylog
import com.oven.phonelocker.activity.toast
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.customview.FlyView
import com.oven.phonelocker.entity.AppinfoEntity
import com.oven.phonelocker.utils.BoxHelper
import com.oven.phonelocker.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*


/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/27.
 */
class MyService : AccessibilityService(), View.OnClickListener {

    var calendar: Calendar? = null
    var alertView: FlyView? = null
    var anim: ObjectAnimator? = null
    override fun onCreate() {
        super.onCreate()
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
        alertView = FlyView(this)
        alertView?.apply {
            this.listener = flyViewListener
            changeFlyViewText()
            var layoutParams = initLayoutParam()
            this.setLayoutParam(layoutParams)
            windowManager?.addView(this, layoutParams)
        }
    }

    private fun changeFlyViewText(s: String? = "") {
        alertView?.findViewById<TextView>(R.id.alert_view_tv)?.text = "๑乛◡乛๑ $s"
    }

    private val flyViewListener: FlyView.FlyViewListener = object : FlyView.FlyViewListener {
        override fun onLongClick(view: FlyView) {
        }

        override fun onClick(view: FlyView) {
            toast("∑(っ°Д°;)っ\n点我干嘛,快点学习啊")
        }

        override fun onMove(view: FlyView) {
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
        params.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.x = 0
        params.y = 0
        // window size
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        return params
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.apply {
            //            mylog("eventtype: ${AccessibilityEvent.eventTypeToString(eventType)}")
//            mylog("event.classname!!!: ${event.className}")
            mylog("eventtostring ${event.toString()}")
            autoSign(event)
            when (eventType) {
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {//通知
                    isSignAlarm(event)
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    doAppController(event)
                }
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {//小米手机上闹铃在解锁屏幕状态下是没有notification的
                    isSignAlarm(event)
                }
            }
        }
    }

    /**
     * 判断当前是否是签到的闹铃
     *
     * @author zhoupan
     * Created at 2019/10/11 13:00
     */
    private fun isSignAlarm(event: AccessibilityEvent) {
        if (event.packageName == "com.android.deskclock") {
            event.text?.forEach {
                if (it.contains("打卡啊")) {
                    callApp("com.ultrapower.android.me.ry")
                    return
                }
            }
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
                    "com.ultrapower.android.main.MainActivity",
                    "com.ultrapower.android.main.Main1Activity"
                    -> {//进入首页了之后,点击应用
                        performClick("bottomBar2", info)
                        Handler().postDelayed({
                            performClick("item_recycler_app", info)//点击地图签到
                            info.recycle()
                        }, 2000)
                    }
                    "com.ultrapower.oatimer.ui.OATimerActivity" -> {//进入地图页面
                        Handler().postDelayed({
                            performClick("ll_clock_button", info)
                            info.recycle()
                        }, 30000)//30s之后再进行签到操作
                    }
                }
            }
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
        datalist?.forEach {
            if (it.packageName.toString() == event.packageName.toString()) {//如果打开了受控制的app,就跳转到一个页面
                calendar = Calendar.getInstance(Locale.CHINA)
                val zoneStrArr = it.allowTimeZone?.split(",")
                val df = SimpleDateFormat("HH:mm")
                val startTime = df.parse(zoneStrArr?.get(0)!!)
                val endTime = df.parse(zoneStrArr?.get(1)!!)
                val nowTimeStr = Utils.timeStrMake(
                    calendar?.get(Calendar.HOUR_OF_DAY) ?: 0
                ) + ":" + Utils.timeStrMake(calendar?.get(Calendar.MINUTE) ?: 0)
                val nowTime = df.parse(nowTimeStr)
                mylog("判断的当前时间 : $nowTimeStr")
                if (nowTime.time in startTime.time..endTime.time) {//如果在限定时间内 就不去处理剩余可以玩时间了..
                    mylog("在时间内，耍吧你就")
                    it.limitTime = System.currentTimeMillis()
                    return
                }
                if (System.currentTimeMillis() - it.limitTime > AppCons.USE_TIME_LIMIT) {
                    changeFlyViewText()
                    val i = Intent(this@MyService, KillProcessActivity::class.java)
                    i.putExtra("targetPackageName", event.packageName.toString())
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    return@forEach
                } else {
                    //提示用户当前还有多少分钟可以耍
                    val deadLineTime = AppCons.USE_TIME_LIMIT + it.limitTime
                    changeFlyViewText("到${df.format(Date(deadLineTime))}，就不能玩了啊")
                }
            }
        }
    }

    @Subscribe
    fun onEvent(message: String) {
        when (message.split("|")[0]) {
            AppCons.EB_DB_UPDATE -> {//表示数据更新了
                getDBData()//重新获取一下数据库里面的数据
            }
            AppCons.EB_TO_HOME -> {//回收也
                this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            }
            AppCons.EB_MODIFY_FLY_VIEW_TEXT -> {//更改悬浮窗文字的事件
                changeFlyViewText("${message.split("|")[1]}")
            }
        }
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        toast("服务启动", true)
        getDBData()
//        createAlarm("11:50", AppCons.MODEL_AUTO_LOGIN)
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
     * 打开app
     *
     * @author zhoupan
     * Created at 2019/10/11 9:48
     */
    private fun callApp(packageName: String) {
        try {
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            toast("跳转到app $packageName 咯", true)
        } catch (e: Exception) {
            toast("跳转其他app出现意外 ${e.printStackTrace()}", true)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }

}