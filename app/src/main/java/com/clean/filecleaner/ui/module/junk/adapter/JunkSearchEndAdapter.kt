package com.clean.filecleaner.ui.module.junk.adapter

import android.text.format.Formatter.formatFileSize
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemJunkSearchEndBinding
import com.clean.filecleaner.databinding.ItemJunkSearchEndParentBinding
import com.clean.filecleaner.ext.isGrantAppCache
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.junk.bean.CleanJunkType
import com.clean.filecleaner.ui.module.junk.bean.JunkType
import com.clean.filecleaner.ui.module.junk.bean.TrashItem
import com.clean.filecleaner.ui.module.junk.bean.TrashItemCache
import com.clean.filecleaner.ui.module.junk.bean.TrashItemParent
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid12OrAbove

class JunkSearchEndAdapter(
    private val activity: AppCompatActivity,
    private val source: MutableList<CleanJunkType>, private val onItemChanged: () -> Unit, private val onButtonGrantCLick: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val PARENT_TYPE = 0
        const val ITEM_TYPE = 1
    }

    private val horizontalMargin by lazy { SizeUtils.dp2px(16f) }
    private val verticalMargin by lazy { SizeUtils.dp2px(12f) }

    override fun getItemViewType(position: Int): Int {
        return if (source[position] is TrashItemParent) PARENT_TYPE else ITEM_TYPE
    }

    override fun getItemCount(): Int = source.size

    inner class ParentViewHolder(val viewBinding: ItemJunkSearchEndParentBinding) : RecyclerView.ViewHolder(viewBinding.root)
    inner class ItemViewHolder(val viewBinding: ItemJunkSearchEndBinding) : RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PARENT_TYPE -> ParentViewHolder(ItemJunkSearchEndParentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ItemViewHolder(ItemJunkSearchEndBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ParentViewHolder -> bindParentViewHolder(holder)
            is ItemViewHolder -> bindItemViewHolder(holder)
        }
    }

    private fun bindParentViewHolder(holder: ParentViewHolder) {
        val item = source[holder.layoutPosition] as TrashItemParent
        val bind = holder.viewBinding

        if (JunkType.APP_CACHE == item.trashType && activity.isGrantAppCache().not()) {
            bind.loading.stopRotatingWithRotateAnimation()
            bind.loading.setImageResource(R.drawable.grey_circle)
            bind.btnGrant.isVisible = true
            bind.root.setBackgroundResource(R.drawable.shape_bg_white_8)
        } else {
            bind.loading.isEnabled = if (item.isLoading) {
                false
            } else if (JunkType.APP_CACHE == item.trashType) {
                activity.isGrantAppCache() && item.subItems.isNotEmpty()
            } else {
                true
            }
            bind.root.setBackgroundResource(
                if (item.isOpen) R.drawable.shape_bg_white_top_8 else R.drawable.shape_bg_white_8
            )
            bind.btnGrant.isVisible = false
            if (item.isLoading) {
                bind.loading.setImageResource(R.mipmap.icon_item_loading)
                bind.loading.startRotatingWithRotateAnimation()
            } else {
                bind.loading.stopRotatingWithRotateAnimation()
                bind.loading.setImageResource(
                    if (item.select) R.mipmap.icon_item_checked else R.drawable.grey_circle
                )
            }
        }

        bind.itemExpand.animate()
            .rotation(if (item.isOpen) 180f else 0f)
            .setDuration(270L).start()

        bind.image.setImageResource(item.trashType.iconId)
        bind.image.isVisible = true
        bind.name.text = activity.getString(item.trashType.nameId)
        bind.size.text = if (item.trashType == JunkType.APP_CACHE) {
            formatFileSize(
                activity,
                item.subItems.filterIsInstance<TrashItemCache>().sumOf { it.fileSize }
            )
        } else {
            formatFileSize(
                activity,
                item.subItems.filterIsInstance<TrashItem>().sumOf { it.fileSize }
            )
        }

        setParentItemLayoutParams(bind.root)

        bind.btnGrant.setOnClickListener {
            if (JunkType.APP_CACHE == item.trashType && activity.isGrantAppCache().not()) {
                onButtonGrantCLick()
            }
        }

        bind.loading.setOnClickListener {
            if (JunkType.APP_CACHE == item.trashType && activity.isGrantAppCache().not()) {
                bind.loading.setImageResource(R.drawable.grey_circle)
                onButtonGrantCLick()
                return@setOnClickListener
            }
            item.select = !item.select
            item.subItems.onEach { it.select = item.select }
            notifyItemRangeChanged(holder.layoutPosition, item.subItems.size + 1)
            onItemChanged.invoke()
        }

        holder.itemView.setOnClickListener {
            if (item.isLoading) return@setOnClickListener
            if (JunkType.APP_CACHE == item.trashType && activity.isGrantAppCache().not()) {
                onButtonGrantCLick()
                return@setOnClickListener
            }
            if (JunkType.APP_CACHE == item.trashType && item.subItems.isEmpty()) {
                ToastUtils.showLong(activity.getString(R.string.there_are_no_application_cache_files))
                return@setOnClickListener
            }
            if (item.isOpen) {
                item.isOpen = false
                source.removeAll(item.subItems)
                notifyItemRangeRemoved(holder.layoutPosition + 1, item.subItems.size)
                notifyItemChanged(holder.layoutPosition)
            } else {
                item.isOpen = true
                source.addAll(holder.layoutPosition + 1, item.subItems)
                notifyItemRangeInserted(holder.layoutPosition + 1, item.subItems.size)
                notifyItemChanged(holder.layoutPosition)
            }
        }
    }

    private fun setParentItemLayoutParams(view: View) {
        val lp = view.layoutParams as? RecyclerView.LayoutParams
        lp?.setMargins(horizontalMargin, verticalMargin, horizontalMargin, 0)
    }

    private fun bindItemViewHolder(holder: ItemViewHolder) {
        val item = source[holder.layoutPosition]
        val bind = holder.viewBinding

        when (item) {
            is TrashItemCache -> {
                bind.image.isInvisible = false
                bind.name.text = item.name
                bind.size.text = formatFileSize(activity, item.fileSize)
                bind.loading.setImageResource(if (item.select) R.mipmap.icon_item_checked else R.drawable.grey_circle)
                bind.loading.isEnabled = isAndroid12OrAbove().not()
                bind.image.setImageDrawable(item.drawable)

                val isNeedBottomCorner = isNeedBottomCorner(holder.layoutPosition)
                bind.root.setBackgroundResource(
                    if (isNeedBottomCorner) R.drawable.shape_bg_white_bottom_8 else R.drawable.shape_bg_white_0
                )
                setItemLayoutParams(bind.root)
            }

            is TrashItem -> {
                bind.image.isInvisible = true
                bind.name.text = item.name
                bind.size.text = formatFileSize(activity, item.fileSize)
                bind.loading.setImageResource(if (item.select) R.mipmap.icon_item_checked else R.drawable.grey_circle)
                bind.loading.isEnabled = true
                bind.image.setImageDrawable(null)

                val isNeedBottomCorner = isNeedBottomCorner(holder.layoutPosition)
                bind.root.setBackgroundResource(
                    if (isNeedBottomCorner) R.drawable.shape_bg_white_bottom_8 else R.drawable.shape_bg_white_0
                )
                setItemLayoutParams(bind.root)
            }

            else -> Unit
        }

        bind.loading.setOnClickListener {
            item.select = !item.select
            runCatching {
                val parentIndex = source.indexOfFirst { it.trashType == item.trashType }
                if (parentIndex == -1) return@setOnClickListener
                val parent = source.getOrNull(parentIndex) as? TrashItemParent ?: return@setOnClickListener
                parent.select = parent.subItems.all { it.select }
                notifyItemChanged(parentIndex)
                notifyItemChanged(holder.layoutPosition)
            }
            onItemChanged.invoke()
        }

        holder.itemView.setOnClickListener {
            when (item) {
                is TrashItemCache -> {
                    if (isAndroid12OrAbove().not()) bind.loading.performClick()
                }

                is TrashItem -> {
                    CommonDialog(
                        title = item.name,
                        message = item.path,
                        leftBtn = "",
                        rightBtn = activity.getString(R.string.ok),
                        cancelable = true
                    ).show(activity.supportFragmentManager, "CommonDialog")
                }

                else -> Unit
            }
        }
    }

    private fun setItemLayoutParams(view: View) {
        val lp = view.layoutParams as? RecyclerView.LayoutParams
        lp?.setMargins(horizontalMargin, 0, horizontalMargin, 0)
    }

    private fun isNeedBottomCorner(currentPosition: Int): Boolean {
        return (currentPosition == source.lastIndex) ||
                (source.getOrNull(currentPosition + 1) is TrashItemParent)
    }


}