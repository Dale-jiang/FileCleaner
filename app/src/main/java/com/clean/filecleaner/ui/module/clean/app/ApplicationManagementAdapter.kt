package com.clean.filecleaner.ui.module.clean.app

import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemApplicationManagementBinding
import com.clean.filecleaner.ext.formatTimestampToMMddyyyy
import com.clean.filecleaner.utils.Tools

class ApplicationManagementAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<ApplicationInfo>,
    private val clickListener: (ApplicationInfo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun getItemCount(): Int {
        return list.size
    }

    inner class ItemViewHolder(val binding: ItemApplicationManagementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(ItemApplicationManagementBinding.inflate(LayoutInflater.from(activity), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {

            with(holder.binding) {
                val item = list[holder.layoutPosition]
                Glide.with(activity).load(item.drawable).into(appIcon)
                appName.text = item.appName
                installTime.text = activity.getString(R.string.installed_time, item.installTime.formatTimestampToMMddyyyy())

                sizeUsed.isVisible = item.usedSize > 0
                sizeUsed.text = Formatter.formatFileSize(activity, item.usedSize)

                lastUsed.isVisible = item.lastUsedTime > 0
                if (item.lastUsedTime > 0) {
                    val day = Tools.calculateDaysUnused(item.lastUsedTime).toInt()
                    val preStr = if (day > 1) activity.getString(R.string.days_unused) else activity.getString(R.string.day_unused)
                    val lastUsedStr = if (day > 30) activity.getString(R.string.more_than) + "30$preStr" else "$day$preStr"
                    lastUsed.text = lastUsedStr
                }

                btnUninstall.setOnClickListener {
                    clickListener.invoke(item)
                }

            }
        }

    }

}