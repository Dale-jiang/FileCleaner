package com.clean.filecleaner.ui.module.clean.junk.adapter

import android.app.Activity
import android.text.format.Formatter.formatFileSize
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemJunkSearchBinding
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.module.clean.junk.bean.JunkSearchItem

class JunkSearchAdapter(private val activity: Activity, val list: List<JunkSearchItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun getItemCount(): Int {
        return list.size
    }

    inner class ItemViewHolder(val binding: ItemJunkSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(ItemJunkSearchBinding.inflate(LayoutInflater.from(activity), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {

            with(holder.binding) {
                val item = list[holder.layoutPosition]
                image.setImageResource(item.icon)
                name.text = item.type
                size.text = formatFileSize(activity, item.size)

                if (item.isLoading) {
                    loading.setImageResource(R.mipmap.icon_item_loading)
                    loading.startRotatingWithRotateAnimation(500)
                } else {
                    loading.stopRotatingWithRotateAnimation()
                    loading.setImageResource(R.mipmap.icon_item_checked)
                }
            }
        }

    }

}