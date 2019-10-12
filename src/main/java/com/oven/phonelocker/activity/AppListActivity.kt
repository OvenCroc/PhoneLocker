package com.oven.phonelocker.activity

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
class AppListActivity : BaseActivity(), View.OnClickListener {

    var hasSelectEntityList: MutableList<AppinfoEntity> = mutableListOf()

    var mAdapter: AppListAdapter? = null
    var dataList: MutableList<AppinfoEntity> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_list_activity_layout)
    }

    override fun initView() {
        add_tv.setOnClickListener(this)
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
        mAdapter = AppListAdapter(dataList, true,itemCheckListener)
        mainRc.adapter = mAdapter
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
        }
    }


}