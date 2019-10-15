package com.oven.phonelocker.utils

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.oven.phonelocker.App

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/10/9.
 */
object Utils {
    fun timeStrMake(time: Int): String {
        return if (time in 0 until 10) {
            "0$time"
        } else
            time.toString()
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    fun dp2px(dpValue: Float): Int {
        val scale = App.app?.resources?.displayMetrics?.density ?: 0f
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     *
     * 通过执行百分比值获取当前百分比对应的颜色值
     *
     * @param fraction  执行百分比
     * @param startColor  起始颜色
     * @param endColor    结束颜色
     * @return
     */
    fun getCurrentColor(fraction: Float, startColor: Int, endColor: Int): Int {
        val redCurrent: Int
        val blueCurrent: Int
        val greenCurrent: Int
        val alphaCurrent: Int

        val redStart = Color.red(startColor)
        val blueStart = Color.blue(startColor)
        val greenStart = Color.green(startColor)
        val alphaStart = Color.alpha(startColor)

        val redEnd = Color.red(endColor)
        val blueEnd = Color.blue(endColor)
        val greenEnd = Color.green(endColor)
        val alphaEnd = Color.alpha(endColor)

        val redDifference = redEnd - redStart
        val blueDifference = blueEnd - blueStart
        val greenDifference = greenEnd - greenStart
        val alphaDifference = alphaEnd - alphaStart

        redCurrent = (redStart + fraction * redDifference).toInt()
        blueCurrent = (blueStart + fraction * blueDifference).toInt()
        greenCurrent = (greenStart + fraction * greenDifference).toInt()
        alphaCurrent = (alphaStart + fraction * alphaDifference).toInt()

        return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent)
    }

    /**
     * 测量视图尺寸
     *
     * @param view 视图
     * @return arr[0]: 视图宽度, arr[1]: 视图高度
     */
    fun measureView(view: View): IntArray {
        var lp: ViewGroup.LayoutParams? = view.layoutParams
        if (lp == null) {
            lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val widthSpec = ViewGroup.getChildMeasureSpec(0, 0, lp.width)
        val lpHeight = lp.height
        val heightSpec: Int
        if (lpHeight > 0) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY)
        } else {
            heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        }
        view.measure(widthSpec, heightSpec)
        return intArrayOf(view.measuredWidth, view.measuredHeight)
    }
}