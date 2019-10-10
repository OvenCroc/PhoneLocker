package com.oven.phonelocker.entity

import com.oven.phonelocker.R
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/30.
 *
 *
 *
 * 注意,新增字段的时候,一点要用?= 因为之前数据库里面的值是没有这个字段的如果不允许为空,会崩..
 */
@Entity
data class AppinfoEntity(
    /**
     * id
     */
    @Id
    var id: Long = 0,
    var appName: String = "未知",
    /**
     * 允许使用的时间段,默认使用时段是晚上6点到晚上11点,目前只能有一个..
     */
    var allowTimeZone: String? = "18:00,23:00",
    var iconRes: Int = R.mipmap.ic_launcher,
    var packageName: String = "",
    /**
     * 上一次使用日期
     */
    var lastUseTime: Long = 0,
    /**
     * 单次使用时长
     */
    var wasteTime: Long = 0,
    /**
     * 总共使用时长
     */
    var totalWasteTime: Long = 0,
    /**
     * 选中状态
     */
    var status: Boolean = false,
    /**
     * 上一次的输入了之后的时间状态
     */
    var limitTime: Long = 0L
)
