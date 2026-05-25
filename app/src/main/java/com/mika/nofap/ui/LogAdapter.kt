package com.mika.nofap.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mika.nofap.R
import com.mika.nofap.databinding.ItemLogBinding
import com.mika.nofap.util.StreakLog

class LogAdapter : RecyclerView.Adapter<LogAdapter.VH>() {
    private var items = listOf<StreakLog>()

    fun submitList(newItems: List<StreakLog>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount() = items.size

    class VH(private val b: ItemLogBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(log: StreakLog, rank: Int) {
            b.tvRankNumber.text = rank.toString()
            
            val context = itemView.context
            b.tvLogStreak.text = context.getString(R.string.log_streak_format, log.days, log.hours, log.minutes)
            b.tvLogDate.text = context.getString(R.string.log_date_format, log.dateTime)
            b.tvLogReason.text = log.reason
            
            // Material You Styling
            b.tvRankNumber.setTextColor(context.getColor(R.color.accent_gold))
            b.tvLogStreak.setTextColor(context.getColor(R.color.text_primary))
        }
    }
}
