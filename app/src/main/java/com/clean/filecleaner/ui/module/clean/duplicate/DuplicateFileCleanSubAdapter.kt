package com.clean.filecleaner.ui.module.clean.duplicate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.data.docMatchList
import com.clean.filecleaner.databinding.ItemDuplicateFileSubBinding
import com.clean.filecleaner.ext.getApkLogo
import java.io.File

class DuplicateFileCleanSubAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<DuplicateFileSub>,
    private val clickListener: (DuplicateFileSub) -> Unit,
    private val checkBoxListener: Callback,
) : RecyclerView.Adapter<DuplicateFileCleanSubAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemDuplicateFileSubBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemDuplicateFileSubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        with(holder.binding) {

            list[holder.layoutPosition].let { item ->

                when {

                    item.mimeType.startsWith("image/", true) || item.mimeType.startsWith("video/", true) -> {
                        Glide.with(activity)
                            .load(File(item.filePath))
                            .centerCrop()
                            .placeholder(R.mipmap.mc_file_unknown)
                            .into(image)
                    }

                    docMatchList.any { item.mimeType.equals(it, true) } -> {
                        Glide.with(activity)
                            .load(R.mipmap.mc_file_document)
                            .centerCrop()
                            .placeholder(R.mipmap.mc_file_unknown)
                            .into(image)
                    }

                    item.mimeType.startsWith("application/", true) -> {
                        Glide.with(activity)
                            .load(item.filePath.getApkLogo())
                            .centerCrop()
                            .placeholder(R.mipmap.mc_file_unknown)
                            .into(image)
                    }

                    else -> {
                        Glide.with(activity)
                            .load(R.mipmap.mc_file_unknown)
                            .centerCrop()
                            .placeholder(R.mipmap.mc_file_unknown)
                            .into(image)
                    }
                }

                bottomLine.isVisible = position < list.size - 1
                name.text = item.fileName
                size.text = item.fileSizeStr

                checkbox.setImageResource(if (item.isSelected) R.mipmap.icon_screenshot_checked else R.mipmap.icon_screenshot_unchecked)

                checkbox.setOnClickListener {
                    item.isSelected = !item.isSelected
                    notifyItemChanged(holder.layoutPosition)
                    checkBoxListener()
                }

                root.setOnClickListener {
                    clickListener(item)
                }
            }
        }
    }
}