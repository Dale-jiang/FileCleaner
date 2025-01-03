package com.clean.filecleaner.ui.module.screenshot

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemScreenshotGroupBinding

class ScreenshotCleanGroupAdapter(
    private val activity: AppCompatActivity, private val list: MutableList<ScreenshotCleanParent>, val clickListener: (Boolean) -> Unit
) : RecyclerView.Adapter<ScreenshotCleanGroupAdapter.ItemViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    inner class ItemViewHolder(val binding: ItemScreenshotGroupBinding) : RecyclerView.ViewHolder(binding.root) {

        private val subAdapter = ScreenshotCleanSubAdapter(activity, mutableListOf()) {
            val position = layoutPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = list[position]
                item.isSelected = item.children.all { it.isSelected }
                clickListener.invoke(item.isSelected)
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
        fun bind(item: ScreenshotCleanParent) {
            binding.tvDate.text = item.timeStr
            binding.ivChecked.setImageResource(
                if (item.isSelected) R.mipmap.icon_screenshot_checked else R.mipmap.icon_screenshot_unchecked
            )
            subAdapter.list.apply {
                clear()
                addAll(item.children)
                subAdapter.notifyDataSetChanged()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemScreenshotGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
//        with(holder.binding) {
//            val item = list[holder.layoutPosition]
//            tvDate.text = item.timeStr
//            ivChecked.setImageResource(if (item.isSelected) R.mipmap.icon_screenshot_checked else R.mipmap.icon_screenshot_unchecked)
//
//            val list = item.children
//            val adapter = ScreenshotCleanSubAdapter(activity, list) {
//                if (list.all { it.isSelected }) item.select() else item.deselect()
//                clickListener.invoke(item.isSelected)
//                notifyItemChanged(holder.layoutPosition)
//            }
//            recyclerView.itemAnimator = null
//            recyclerView.adapter = adapter


//        }
    }


}