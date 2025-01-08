package com.clean.filecleaner.ui.module.filemanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.ItemMediaSubBinding
import com.clean.filecleaner.ext.opFile

class ManageMediaSubAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<FileInfo>,
    private val isVideo: Boolean,
    private val clickListener: Callback
) : RecyclerView.Adapter<ManageMediaSubAdapter.ItemViewHolder>() {

    private val size = SizeUtils.dp2px(76f)

    inner class ItemViewHolder(val binding: ItemMediaSubBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemMediaSubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        with(holder.binding) {

            list[holder.layoutPosition].let { item ->

                Glide.with(activity)
                    .load(item.path)
                    .placeholder(R.drawable.shape_bg_white_0)
                    .override(size, size)
//                    .transform(CenterCrop(), RoundedCorners(SizeUtils.dp2px(2f)))
                    .into(image)

                videoPlay.isVisible = isVideo
                tvDuration.isVisible = isVideo
                if (isVideo) tvDuration.text = item.durationStr
                checkbox.setImageResource(if (item.isSelected) R.drawable.svg_image_check else R.drawable.svg_image_uncheck)

                checkbox.setOnClickListener {
                    item.isSelected = !item.isSelected
                    notifyItemChanged(holder.layoutPosition)
                    clickListener()
                }

                image.setOnClickListener {
                    activity.opFile(item.path, item.mimetype)
                }
            }
        }
    }
}