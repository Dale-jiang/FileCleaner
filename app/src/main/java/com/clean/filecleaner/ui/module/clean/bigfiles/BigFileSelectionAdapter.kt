package com.clean.filecleaner.ui.module.clean.bigfiles

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemBigFileSelectionBinding

class BigFileSelectionAdapter(private val context: Context, private val click: (BigFileSelection) -> Unit) : RecyclerView.Adapter<BigFileSelectionAdapter.ViewHolder>() {

    private var mList: MutableList<BigFileSelection> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun initData(list: MutableList<BigFileSelection>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(val binding: ItemBigFileSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigFileSelectionAdapter.ViewHolder {
        return ViewHolder(ItemBigFileSelectionBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: BigFileSelectionAdapter.ViewHolder, position: Int) {
        holder.binding.apply {

            val data = mList[holder.layoutPosition]

            tvItem.setTextColor(ContextCompat.getColor(context, if (data.select) R.color.colorPrimary else R.color.color_text))
            tvItem.text = data.name

            root.setOnClickListener {
                if (data.select) return@setOnClickListener
                mList.forEach { it.select = false }
                data.select = true
                click.invoke(data)
                notifyDataSetChanged()
            }
        }
    }

}