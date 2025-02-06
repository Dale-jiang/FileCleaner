package com.clean.filecleaner.ui.module.clean.empty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.clean.filecleaner.databinding.ItemEmptyFoldersBinding

class EmptyFoldersCleanAdapter(private val activity: AppCompatActivity, val list: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun getItemCount(): Int {
        return list.size
    }

    inner class ItemViewHolder(val binding: ItemEmptyFoldersBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(ItemEmptyFoldersBinding.inflate(LayoutInflater.from(activity), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {

            with(holder.binding) {
                val item = list[holder.layoutPosition]
                path.text = item
            }
        }

    }

}