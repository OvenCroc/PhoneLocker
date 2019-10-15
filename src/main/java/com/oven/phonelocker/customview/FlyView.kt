package com.oven.phonelocker.customview

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Vibrator
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.noober.background.drawable.DrawableCreator
import com.oven.phonelocker.R
import com.oven.phonelocker.activity.mylog
import com.oven.phonelocker.activity.toast
import com.oven.phonelocker.utils.Utils
import kotlin.math.abs
import kotlin.math.absoluteValue


/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/10/11.
 */
class FlyView : LinearLayout {
    private var mLastDownX = 0
    private var mLastDownY = 0
    private var mTouchSlop: Float = 0f
    private var mView: View? = null
    private var mVibrator: Vibrator? = null
    private var mLastDownTime: Long = 0
    /**
     * 是否触发了长按
     */
    private var isLongClickNow = false
    /**
     * 是否还按在屏幕上
     */
    private var isTouching = false
    var listener: FlyViewListener? = null
    /**
     * 长按时间
     */
    private val LONG_CLICK_LIMIT = 500L
    /**
     * 单击时间
     */
    private val CLICK_LIMIT = 200L

    private lateinit var windowManger: WindowManager
    private var mLayoutParams: WindowManager.LayoutParams? = null
    private var lastBackgroundColor = 0

    constructor(context: Context) : super(context) {
        initFun(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initFun(context)
    }

    private fun initFun(context: Context) {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        windowManger = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mView = LayoutInflater.from(context).inflate(R.layout.add_view_layout, null)
        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //实现在size改变的时候有个改变的过渡
        layoutTransition = LayoutTransition()
        layoutTransition?.enableTransitionType(LayoutTransition.CHANGING)
        setFlyBackgroundColor(ContextCompat.getColor(context, R.color.app_theme))
        this.addView(mView)
    }

    fun setLayoutParam(params: WindowManager.LayoutParams) {
        this.mLayoutParams = params
    }

    fun moveFlyView(x: Int, y: Int, isAnim: Boolean? = false) {
        mLayoutParams?.apply {
            if (isAnim!!) {
                val valueAnimator = ValueAnimator.ofInt(this.x, x)
                val valueAnimator2 = ValueAnimator.ofInt(this.y, y)
                val animatorSet = AnimatorSet()
                animatorSet.duration = 2000
                animatorSet.interpolator = DecelerateInterpolator()
                animatorSet.play(valueAnimator).with(valueAnimator2)
            } else {
                this.x = x
                this.y = y
                windowManger.updateViewLayout(this@FlyView, this)
            }
        }
    }

    /**
     * 设置背景色，默认是又圆角的
     *
     * @author zhoupan
     * Created at 2019/10/12 12:00
     */
    fun setFlyBackgroundColor(color: Int, duration: Long? = 500) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener {
            val drawable = DrawableCreator.Builder()
                .setCornersRadius(Utils.dp2px(context.resources.getDimension(R.dimen.normal_btn_radius)).toFloat())
                .setSolidColor(
                    Utils.getCurrentColor(
                        it.animatedFraction,
                        lastBackgroundColor,
                        color
                    )
                ).build()
            this.background = drawable
            if (it.animatedFraction == 1f) {
                lastBackgroundColor = color
            }
        }
        animator.duration = duration!!
        animator.start()
    }


    /**
     * 判断是否是轻微滑动
     *
     * @param event
     * @return
     */
    private fun isTouchSlop(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        return abs(x - mLastDownX) < mTouchSlop && abs(y - mLastDownY) < mTouchSlop
    }

    /**
     * 判断是否是单击
     *
     * @param event
     * @return
     */
    private fun isClick(event: MotionEvent): Boolean {
        val offsetX = abs(event.x - mLastDownX)
        val offsetY = abs(event.y - mLastDownY)
        val time = System.currentTimeMillis() - mLastDownTime
        return offsetX < mTouchSlop * 2 && offsetY < mTouchSlop * 2 && time < CLICK_LIMIT
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            when (this.action) {
                MotionEvent.ACTION_DOWN -> {//点下的时候
                    //记录当前按下的时间,用来判断是否是点击
                    mLastDownTime = System.currentTimeMillis()
                    //记录当前的位置
                    mLastDownX = x.toInt()
                    mLastDownY = y.toInt()
                    Handler().postDelayed({
                        if ((mLastDownTime - System.currentTimeMillis()).absoluteValue >= LONG_CLICK_LIMIT && isTouchSlop(
                                event
                            ) && isTouching
                        ) {//如果当前任然触摸着屏幕,并且时间大于等于长按时间,说嘛正在长按
                            mVibrator?.vibrate(100)
                            context.toast("可以移动手指改变位置了哦", true)
                            listener?.onLongClick(this@FlyView)
                            isLongClickNow = true//触发了长按开关
                        }
                    }, LONG_CLICK_LIMIT)
                    isTouching = true//down的时候就是在touch的时候
                }
                MotionEvent.ACTION_MOVE -> {//移动
                    if (isTouchSlop(event)) {//如果是轻微滑动,直接不执行后面的操作
                        return super.onTouchEvent(event)
                    }
                    if (isLongClickNow) {
                        moveFlyView(
                            (event.rawX).toInt() - measuredWidth / 2,
                            (event.rawY).toInt() - measuredHeight
                        )
                        windowManger.updateViewLayout(this@FlyView, mLayoutParams)
                        mylog("开始位移啦")
                        listener?.onMove(this@FlyView)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick(event)) {
                        listener?.onClick(this@FlyView)
                    } else {
                        listener?.onUp(this@FlyView)
                    }
                    isLongClickNow = false
                    isTouching = false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    open interface FlyViewListener {
        fun onLongClick(view: FlyView)
        fun onClick(view: FlyView)
        fun onMove(view: FlyView)
        fun onUp(view: FlyView)
    }
}
