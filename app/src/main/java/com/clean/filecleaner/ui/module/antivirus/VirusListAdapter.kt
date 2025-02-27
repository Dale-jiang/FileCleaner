package com.clean.filecleaner.ui.module.antivirus

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ItemVirusInfoBinding
import com.clean.filecleaner.ui.module.dialog.CommonDialog

class VirusListAdapter(
    private val activity: AppCompatActivity,
    val list: MutableList<VirusInfo>,
    private val clickListener: (VirusInfo) -> Unit
) :
    RecyclerView.Adapter<VirusListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemVirusInfoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemVirusInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[holder.layoutPosition]

            if (item.isApp) {
                Glide.with(activity).load(item.drawable).into(image)
            } else {
                image.setImageResource(R.drawable.icon_virus_files)
            }

            name.text = item.label
            btnStop.text = if (item.isApp) activity.getString(R.string.uninstall) else activity.getString(R.string.delete)
            btnStop.setOnClickListener {
                clickListener.invoke(item)
            }

            root.setOnClickListener {
                CommonDialog(
                    title = activity.getString(R.string.virus),
                    message = item.path,
                    rightBtn = activity.getString(R.string.ok),
                    cancelable = true,
                    rightClick = { }
                ).show(activity.supportFragmentManager, "CommonDialog")

            }

        }
    }

    override fun getItemCount() = list.size

}