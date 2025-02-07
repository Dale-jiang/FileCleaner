package com.clean.filecleaner.ui.module.clean.bigfiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemBigFilesBinding
import com.clean.filecleaner.ext.getApkLogo
import com.clean.filecleaner.ui.module.clean.bigfiles.viewmodel.BigFilesHelper
import com.clean.filecleaner.ui.module.filemanager.FileInfo

class BigFilesAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<FileInfo>,
    private val clickListener: (FileInfo) -> Unit,
    private val checkboxListener: (FileInfo, Boolean) -> Unit
) : RecyclerView.Adapter<BigFilesAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemBigFilesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemBigFilesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        with(holder.binding) {

            list[holder.layoutPosition].let { item ->

                if (BigFilesHelper.isDocument(item.filetype!!)) {
                    Glide.with(activity)
                        .load(R.mipmap.mc_file_document)
                        .centerCrop()
                        .into(image)
                } else if (item.filetype == FileTypes.TYPE_IMAGE || item.filetype == FileTypes.TYPE_VIDEO) {

                    Glide.with(activity)
                        .load(item.path)
                        .placeholder(R.drawable.shape_bg_white_0)
                        .into(image)
                } else if (item.filetype == FileTypes.TYPE_APK) {
                    Glide.with(activity)
                        .load(item.path.getApkLogo())
                        .centerCrop()
                        .placeholder(R.mipmap.mc_file_script)
                        .into(image)
                } else if (item.filetype == FileTypes.TYPE_AUDIO) {
                    Glide.with(activity)
                        .load(R.mipmap.mc_file_audio)
                        .centerCrop()
                        .into(image)
                } else {
                    Glide.with(activity)
                        .load(R.mipmap.mc_file_unknown)
                        .centerCrop()
                        .into(image)
                }

                ivPlay.isVisible = item.filetype == FileTypes.TYPE_VIDEO
                bottomLine.isVisible = position < list.size - 1
                name.text = item.name
                size.text = item.sizeString

                checkbox.isChecked = item.isSelected

                checkbox.setOnClickListener {
                    item.isSelected = checkbox.isChecked
                    notifyItemChanged(holder.layoutPosition)
                    checkboxListener(item, checkbox.isChecked)
                }

                itemView.setOnClickListener {
                    clickListener(item)
                }
            }
        }
    }
}