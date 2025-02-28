package com.clean.filecleaner.ui.module.filemanager.apk

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.ItemManageDocsBinding
import com.clean.filecleaner.ext.getApkLogo
import com.clean.filecleaner.ui.module.filemanager.FileInfo

class ManageAPKAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<FileInfo>,
    private val clickListener: (FileInfo) -> Unit,
    private val checkboxListener: Callback
) : RecyclerView.Adapter<ManageAPKAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemManageDocsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemManageDocsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        with(holder.binding) {

            list[holder.layoutPosition].let { item ->

                Glide.with(activity)
                    .load(item.path.getApkLogo())
                    .centerCrop()
                    .placeholder(R.mipmap.mc_file_script)
                    .into(image)

                bottomLine.isVisible = position < list.size - 1
                name.text = item.name
                size.text = item.sizeString
                checkbox.setImageResource(if (item.isSelected) R.mipmap.icon_screenshot_checked else R.mipmap.icon_screenshot_unchecked)

                checkbox.setOnClickListener {
                    item.isSelected = !item.isSelected
                    notifyItemChanged(holder.layoutPosition)
                    checkboxListener()
                }

                itemView.setOnClickListener {
                    clickListener(item)
                }
            }
        }
    }
}