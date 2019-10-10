package com.oven.phonelocker.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.oven.phonelocker.R
import com.oven.phonelocker.entity.AppinfoEntity
import com.oven.phonelocker.impl.CheckListener

/**
 * description: PhoneLocker
 * Created by xm zhoupan on 2019/9/30.
 */
class AppListAdapter(
    val datalist: MutableList<AppinfoEntity>,
    var isShowCheckBox: Boolean? = false,
    val checkBoxListener: CheckListener? = object : CheckListener {
        override fun onChecked(position: Int, isChecked: Boolean) {

        }
    }
) :
    BaseItemDraggableAdapter<AppinfoEntity, BaseViewHolder>(
        R.layout.app_list_item_layout,
        datalist
    ) {
    override fun convert(helper: BaseViewHolder?, item: AppinfoEntity?) {
        helper?.apply {
            Glide.with(this@AppListAdapter.mContext)
                .load(mContext.packageManager.getApplicationIcon(item?.packageName))
                .into(getView(R.id.app_icon_img))
            setText(
                R.id.app_name_tv,
                item?.appName
            )
            val checkBox = getView<CheckBox>(R.id.status_cbox)
            if (isShowCheckBox!!) {
                checkBox.visibility = View.VISIBLE
                checkBox.isChecked = item?.status ?: false
                checkBox.setOnCheckedChangeListener(object :
                    CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        checkBoxListener?.onChecked(layoutPosition, isChecked)
                    }
                })
            } else {
                checkBox.visibility = View.GONE
            }
        }
    }
}