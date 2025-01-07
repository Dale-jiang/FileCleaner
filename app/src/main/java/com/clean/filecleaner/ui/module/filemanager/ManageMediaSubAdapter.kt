package com.clean.filecleaner.ui.module.filemanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.ItemMediaSubBinding
import com.clean.filecleaner.ext.opFile
import java.io.File

class ManageMediaSubAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<FileInfo>,
    private val isVideo: Boolean,
    private val clickListener: Callback
) : RecyclerView.Adapter<ManageMediaSubAdapter.ItemViewHolder>() {

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
                    .load(File(item.path))
                    .transform(CenterCrop(), RoundedCorners(SizeUtils.dp2px(2f)))
                    .into(image)

                videoPlay.isVisible = isVideo
                checkbox.setImageResource(if (item.isSelected) R.drawable.svg_image_check else R.drawable.svg_image_uncheck)

                checkbox.setOnClickListener {
                    item.isSelected = !item.isSelected
                    clickListener()
                }

                image.setOnClickListener {
                    activity.opFile(item.path, item.mimetype)
                }
            }
        }
    }
}