package com.clean.filecleaner.ui.module.clean.recent.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemAppLaunchBinding
import com.clean.filecleaner.ext.formatToDuration
import com.clean.filecleaner.ui.module.clean.recent.AppScreenTimeInfo

class ScreenTimeAdapter(private val context: Context, private val callback: (AppScreenTimeInfo) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mList = mutableListOf<AppScreenTimeInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun initList(list: MutableList<AppScreenTimeInfo>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(val binding: ItemAppLaunchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemAppLaunchBinding.inflate(LayoutInflater.from(context), parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            mList[holder.layoutPosition].let {data->
                holder.binding.apply {

                    Glide.with(context)
                        .load(data.icon)
                        .placeholder(R.mipmap.mc_file_unknown)
                        .into(image)

                    size.text = data.duration.formatToDuration()
                    name.text = data.appName
                    btnStop.text = context.getString(R.string.manage)
                    btnStop.setOnClickListener { callback(data) }
                }
            }

        }
    }


}