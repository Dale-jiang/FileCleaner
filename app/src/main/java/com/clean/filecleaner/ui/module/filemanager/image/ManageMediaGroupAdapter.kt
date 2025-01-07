package com.clean.filecleaner.ui.module.filemanager.image

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemMediaGroupBinding
import com.clean.filecleaner.ui.module.clean.screenshot.ManageMediaSubAdapter
import com.clean.filecleaner.ui.module.filemanager.MediaInfoParent

class ManageMediaGroupAdapter(
    private val activity: AppCompatActivity, val list: MutableList<MediaInfoParent>, private val isVideo: Boolean, val clickListener: () -> Unit
) : RecyclerView.Adapter<ManageMediaGroupAdapter.ItemViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    inner class ItemViewHolder(val binding: ItemMediaGroupBinding) : RecyclerView.ViewHolder(binding.root) {

        private val subAdapter = ManageMediaSubAdapter(activity, mutableListOf(), isVideo) {
            val position = layoutPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = list[position]
                item.isSelected = item.children.all { it.isSelected }
                clickListener.invoke()
                notifyItemChanged(position)
            }
        }

        init {
            with(binding.recyclerView) {
                adapter = subAdapter
                setRecycledViewPool(viewPool)
                setHasFixedSize(true)
                itemAnimator = null
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: MediaInfoParent) {
            binding.tvDate.text = item.timeStr
            binding.ivChecked.setImageResource(
                if (item.isSelected) R.mipmap.icon_screenshot_checked else R.mipmap.icon_screenshot_unchecked
            )
            subAdapter.list.apply {
                clear()
                addAll(item.children)
                subAdapter.notifyDataSetChanged()
            }

            binding.ivChecked.setOnClickListener {
                item.isSelected = !item.isSelected
                subAdapter.list.forEach { it.isSelected = item.isSelected }
                notifyItemChanged(layoutPosition)
                subAdapter.notifyDataSetChanged()
                clickListener.invoke()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemMediaGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }


}