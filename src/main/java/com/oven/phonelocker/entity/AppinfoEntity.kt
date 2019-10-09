package com.oven.phonelocker.entity

import com.oven.phonelocker.R
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/30.
 */
@Entity
data class AppinfoEntity(
    /**
     * id
     */
    @Id
    var id: Long = 0,
    var appName: String = "未知",
    var iconRes: Int = R.mipmap.ic_launcher,
    var packageName: String = "",
    /**
     * 选中状态
     */
    var status: Boolean = false,
    /**
     * 上一次的输入了之后的时间状态
     */
    var limitTime: Long = 0L
)
