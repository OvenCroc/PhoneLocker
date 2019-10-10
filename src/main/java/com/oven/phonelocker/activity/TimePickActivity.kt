package com.oven.phonelocker.activity

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import com.oven.phonelocker.R
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.utils.SPUtils
import com.oven.phonelocker.utils.Utils
import kotlinx.android.synthetic.main.time_pick_activity_layout.*
import java.util.*


/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/30.
 */
class TimePickActivity : BaseActivity(), View.OnClickListener {
    var calendar = Calendar.getInstance(Locale.CHINA)
    /**
     * 模式开始时间
     */
    var startTime: Long = 0
    /**
     * 模式结束时间
     */
    var endTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.time_pick_activity_layout)
    }

    override fun initView() {
        initToolBar()
        pick_time_tv.setOnClickListener(this)
        pick_end_time_tv.setOnClickListener(this)
    }

    private fun initToolBar() {
        time_pick_tool_bar.title = "请选择作死模式时间"
        setSupportActionBar(time_pick_tool_bar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        time_pick_tool_bar.setNavigationOnClickListener(this)
    }

    override fun initData() {

    }

    override fun onClick(v: View?) {
        when (v) {
            pick_time_tv -> {
                val startTimeSp = SPUtils.getInstance().getString(AppCons.sp_start_time)
                if (startTimeSp.isNullOrBlank().not()) {
                    showPickTimeDialog(
                        v,
                        startTimeSp.split(":")[0].toInt(),
                        startTimeSp.split(":")[1].toInt()
                    )
                }
            }
            pick_end_time_tv -> {
                val endTimeSp = SPUtils.getInstance().getString(AppCons.sp_end_time)
                if (endTimeSp.isNullOrBlank().not()) {
                    showPickTimeDialog(
                        v,
                        endTimeSp.split(":")[0].toInt(),
                        endTimeSp.split(":")[1].toInt()
                    )
                }
            }
        }
    }

    private fun showPickTimeDialog(
        v: View?,
        defaultHour: Int? = calendar.get(Calendar.HOUR_OF_DAY),
        defaultMinute: Int? = calendar.get(Calendar.MINUTE)
    ) {
        TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val timeStr = Utils.timeStrMake(hourOfDay) + ":" + Utils.timeStrMake(minute)
                if (v == pick_time_tv) {
                    //设置开始时间,要存本地的sp
                    SPUtils.getInstance().put(AppCons.sp_start_time, timeStr)
                    //如果现在已经开始了模式,更新模式的时间
                } else {
                    //结束时间
                    SPUtils.getInstance().put(AppCons.sp_end_time, timeStr)
                    //如果现在已经开始了模式,更新模式的时间
                }
                toast("picktime: $timeStr")
            },
            defaultHour!!,
            defaultMinute!!,
            false
        ).show()
    }

}