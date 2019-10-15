package com.oven.phonelocker.activity

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.oven.phonelocker.R
import com.oven.phonelocker.adapter.AppListAdapter
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.entity.AppinfoEntity
import com.oven.phonelocker.impl.CheckListener
import com.oven.phonelocker.utils.BoxHelper
import kotlinx.android.synthetic.main.app_list_activity_layout.*
import org.greenrobot.eventbus.EventBus

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/29.
 */
class AppListActivity : BaseActivity(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
    TextView.OnEditorActionListener {
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        when (actionId) {
            EditorInfo.IME_ACTION_SEARCH -> {
                doSearch(v?.text)
                return true
            }
            else -> {
                return false
            }
        }
    }

    var hasAddOnScrollLinstener = false

    private fun doSearch(text: CharSequence?) {
        if (text?.isNullOrBlank()?.not() ?: false) {
            mAdapter?.setNewData(dataList?.filter {
                it.appName.contains(text ?: "")
            })
            search_group.visibility = View.GONE
        } else {
            mAdapter?.setNewData(dataList)
            search_group.visibility = View.VISIBLE
        }
    }

    override fun onRefresh() {
        Handler().postDelayed({
            if (swipe.isRefreshing) {
                swipe.isRefreshing = false
            }
        }, 1000)
    }

    var hasSelectEntityList: MutableList<AppinfoEntity> = mutableListOf()

    var mAdapter: AppListAdapter? = null
    var dataList: MutableList<AppinfoEntity> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_list_activity_layout)
    }

    override fun initView() {
        add_tv.setOnClickListener(this)
        swipe.setOnRefreshListener(this)
        clear_search_content_img.setOnClickListener(this)
        search_et.setOnEditorActionListener(this)
        initToolbar()
        initRc()
    }

    private fun initToolbar() {
        list_tool_bar.title = "已安装应用"
        setSupportActionBar(list_tool_bar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        list_tool_bar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initRc() {
        mainRc.layoutManager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        mainRc.addItemDecoration(decoration)
        mAdapter = AppListAdapter(dataList, true, itemCheckListener)
        mainRc.adapter = mAdapter

        mainRc.viewTreeObserver.addOnGlobalLayoutListener {
            if (!hasAddOnScrollLinstener) {
                mainRc.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy >= 0 && mAdapter?.data?.size != 1) {//表示往下滑,显示搜索框
                            search_group.visibility = View.VISIBLE
                        } else {
                            search_group.visibility = View.GONE
                        }
                    }
                })
                hasAddOnScrollLinstener = true
            }
        }
    }

    override fun initData() {
        getAppList()
    }

    private val itemCheckListener: CheckListener = object : CheckListener {
        override fun onChecked(position: Int, isChecked: Boolean) {
            mAdapter?.apply {
                if (this.datalist.size > 0) {
                    val entity = datalist[position]
                    entity.status = isChecked
                    if (isChecked && !hasSelectEntityList.contains(entity)) {//没有选择过,添加到list
                        hasSelectEntityList.add(entity)
                    }
                    if (!isChecked && hasSelectEntityList.contains(entity)) {//已经选择过了,从list中删除
                        hasSelectEntityList.remove(entity)
                    }
                }
            }
        }
    }

    /**
     * 获取用户已安装app列表
     *
     * @author zhoupan
     * Created at 2019/9/30 9:41
     */
    private fun getAppList() {
        val appList =
            (packageManager.getInstalledPackages(0)
                ?.filter {
                    it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM === 0
                } ?: mutableListOf())//非系统app
        dataList.clear()
        appList?.forEach {
            it.applicationInfo.loadIcon(packageManager)
            dataList.add(
                AppinfoEntity(
                    appName = it.applicationInfo.loadLabel(packageManager).toString(),
                    packageName = it.applicationInfo.packageName.toString()
                )
            )
        }


        mAdapter?.setNewData(dataList)
    }

    override fun onClick(v: View?) {
        when (v) {
            add_tv -> {
                val boxStore = BoxHelper.boxStore
                hasSelectEntityList?.forEach {
                    boxStore?.boxFor(AppinfoEntity::class.java)?.put(it)
                }
                EventBus.getDefault().post(AppCons.EB_DB_UPDATE)
                toast("添加成功")
                finish()
            }
            clear_search_content_img -> {//清空搜索内容
                search_et.setText("")
            }
        }
    }


}