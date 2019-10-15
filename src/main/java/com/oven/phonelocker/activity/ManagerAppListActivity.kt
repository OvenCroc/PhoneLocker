package com.oven.phonelocker.activity

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import com.oven.phonelocker.R
import com.oven.phonelocker.adapter.AppListAdapter
import com.oven.phonelocker.common.AppCons
import com.oven.phonelocker.entity.AppinfoEntity
import com.oven.phonelocker.utils.BoxHelper
import kotlinx.android.synthetic.main.manager_app_list_activity_layout.*
import org.greenrobot.eventbus.EventBus

/**
 * description:管理已添加app页面
 * Created by xm zhoupan on 2019/10/8.
 */
class ManagerAppListActivity : BaseActivity(), OnItemSwipeListener,
    BaseQuickAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        Handler().postDelayed({
            if (swipeLayout.isRefreshing) {
                swipeLayout.isRefreshing = false
            }
        }, 1000)
    }

    var datalist = mutableListOf<AppinfoEntity>()
    var adapter: AppListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_app_list_activity_layout)
    }

    override fun initView() {
        swipeLayout.setOnRefreshListener(this)
        initToolBar()
        initRc()
    }

    private fun initRc() {
        mainRc.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter(datalist)
        val itemTouchHelper = ItemTouchHelper(ItemDragAndSwipeCallback(adapter))
        itemTouchHelper.attachToRecyclerView(mainRc)
        adapter?.enableSwipeItem()
        adapter?.setOnItemSwipeListener(this)
        adapter?.onItemClickListener = this
        val decoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        mainRc.addItemDecoration(decoration)
        mainRc.adapter = adapter
    }

    private fun initToolBar() {
        manager_app_tool_bar.title = "已控制应用"
        setSupportActionBar(manager_app_tool_bar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        manager_app_tool_bar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun initData() {
        //从数据库获取数据
        datalist = (BoxHelper.getList(AppinfoEntity::class.java)
            ?: mutableListOf()) as MutableList<AppinfoEntity>
        if (!datalist.isNullOrEmpty()) {//数据不为空的话
            adapter?.setNewData(datalist)
        }
    }

    override fun clearView(p0: RecyclerView.ViewHolder?, p1: Int) {
    }

    override fun onItemSwiped(p0: RecyclerView.ViewHolder?, p1: Int) {
        if (!datalist.isNullOrEmpty() && datalist.size > p1) {
            //删除数据库里面的数据
            BoxHelper.boxStore?.boxFor(AppinfoEntity::class.java)?.remove(datalist[p1])
            //通知service里面更新数据
            EventBus.getDefault().post(AppCons.EB_DB_UPDATE)
        }
    }

    override fun onItemSwipeStart(p0: RecyclerView.ViewHolder?, p1: Int) {
    }

    override fun onItemSwipeMoving(
        p0: Canvas?,
        p1: RecyclerView.ViewHolder?,
        p2: Float,
        p3: Float,
        p4: Boolean
    ) {
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        if (datalist.isNullOrEmpty().not() && datalist.size > position) {
            val i = Intent(this@ManagerAppListActivity, AppDetailActivity::class.java)
            this@ManagerAppListActivity.startActivity(i)
        }
    }


}