package com.oven.phonelocker.common

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/29.
 */
object AppCons {
    const val IS_DEBUG = true

    //sp_key
    const val sp_start_time = "start_time"
    const val sp_end_time = "end_time"
    const val sp_status = "status"

    //receiver action
    const val MODEL_START = "start_model"//模式开始
    const val MODEL_PAUSE = "pause_model"//模式暂停
    const val MODEL_RESUME = "resume_model"//模式继续
    const val MODEL_END = "end_model"//模式解除

    //const value
    const val USE_TIME_LIMIT = 10 * 60 * 1000//使用时间暂定是10分钟

    //eventbus order
    const val EB_DB_UPDATE = "db_update"
}