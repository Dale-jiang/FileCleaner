package com.clean.filecleaner.ui.module.clean.recent.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemAppLaunchBinding
import com.clean.filecleaner.ui.module.clean.recent.AppLaunchInfo
import com.clean.filecleaner.ui.module.clean.recent.LaunchType
import com.clean.filecleaner.ui.module.clean.recent.RecentAppHelper.isAppEnableStop

class LaunchesAdapter(private val context: Context, private val callback: (AppLaunchInfo) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var launchType: LaunchType = LaunchType.TOTAL
    val mList = mutableListOf<AppLaunchInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun initList(launchType: LaunchType, list: MutableList<AppLaunchInfo>) {
        this.launchType = launchType
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
            val data = mList[holder.layoutPosition]
            val (launchCount, launchString) = getLaunchData(data)
            holder.binding.apply {
                Glide.with(context)
                    .load(data.icon)
                    .placeholder(R.mipmap.mc_file_unknown)
                    .into(image)

                name.text = data.appName
                val launchStr = "$launchCount $launchString"
                size.text = launchStr

                btnStop.isEnabled = isAppEnableStop(data.packageName)
                btnStop.setOnClickListener { callback(data) }
            }
        }
    }

    private fun getLaunchData(data: AppLaunchInfo): Pair<Int, String> {
        val launchCount: Int = when (launchType) {
            LaunchType.TOTAL -> {
                data.totalCount
            }

            LaunchType.FOREGROUND -> {
                data.foreground
            }

            LaunchType.BACKGROUND -> {
                data.background
            }
        }
        val launchString: String = if (launchCount <= 1) context.getString(R.string.launch) else context.getString(R.string.launches)
        return Pair(launchCount, launchString)
    }

//    AppLaunchInfo(
//    AppUtils.getAppName(packageName),
//    packageName,
//    AppUtils.getAppIcon(packageName),
//    totalLaunches,
//    events.size,
//    totalLaunches - events.size
//    )

}