package com.clean.filecleaner.ui.module.filemanager.docs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.ItemDuplicateFileSubBinding
import com.clean.filecleaner.databinding.ItemManageDocsBinding
import com.clean.filecleaner.databinding.ItemScreenshotSubBinding
import java.io.File

class ManageDocsAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<String>,
    private val clickListener: Callback
) : RecyclerView.Adapter<ManageDocsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemManageDocsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemManageDocsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        with(holder.binding) {

//            list[holder.layoutPosition].let { item ->
//
//                Glide.with(activity)
//                    .load(File(item.path))
//                    .transform(CenterCrop(), RoundedCorners(2))
//                    .into(image)
//
//                checkedView.isVisible = item.isSelected
//
//                image.setOnClickListener {
//                    if (item.isSelected) item.deselect() else item.select()
//                    clickListener.invoke()
//                }
//            }
        }
    }
}