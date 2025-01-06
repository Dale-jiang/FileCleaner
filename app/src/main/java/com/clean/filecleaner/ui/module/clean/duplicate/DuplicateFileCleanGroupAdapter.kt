package com.clean.filecleaner.ui.module.clean.duplicate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.ItemDuplicateFileGroupBinding

class DuplicateFileCleanGroupAdapter(
    private val activity: AppCompatActivity, val list: MutableList<DuplicateFileGroup>, private val changeListener: Callback, val clickListener: (DuplicateFileSub) -> Unit
) : RecyclerView.Adapter<DuplicateFileCleanGroupAdapter.ItemViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    inner class ItemViewHolder(val binding: ItemDuplicateFileGroupBinding) : RecyclerView.ViewHolder(binding.root) {

        private val subAdapter = DuplicateFileCleanSubAdapter(activity, mutableListOf(),
            clickListener = {
                clickListener(it)
            }, checkBoxListener = {
                changeListener()
            })

        init {
            with(binding.recyclerView) {
                adapter = subAdapter
                setRecycledViewPool(viewPool)
                setHasFixedSize(true)
                itemAnimator = null
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: DuplicateFileGroup) {
            subAdapter.list.apply {
                clear()
                addAll(item.children)
                subAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemDuplicateFileGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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