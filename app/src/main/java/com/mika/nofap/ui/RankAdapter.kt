package com.mika.nofap.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mika.nofap.R
import com.mika.nofap.databinding.ItemRankBinding
import com.mika.nofap.util.HapticHelper

data class RankItem(val name: String, val days: Int, val isUnlocked: Boolean)

class RankAdapter : RecyclerView.Adapter<RankAdapter.VH>() {
    private var items = listOf<RankItem>()

    fun submitList(newItems: List<RankItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemRankBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class VH(private val b: ItemRankBinding) : RecyclerView.ViewHolder(b.root) {
        init {
            b.root.setOnClickListener { HapticHelper.click(it) }
        }

        fun bind(item: RankItem) {
            b.tvRankName.text = item.name
            b.tvRankDays.text = "${item.days} ngày"
            
            val context = itemView.context
            if (item.isUnlocked) {
                // Use theme attributes for primary/accent colors
                val typedValue = android.util.TypedValue()
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                val colorPrimary = typedValue.data
                
                b.tvRankName.setTextColor(colorPrimary)
                b.dotRank.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2ECC71")) // Success green
                b.ivCheck.visibility = View.VISIBLE
                b.ivCheck.imageTintList = ColorStateList.valueOf(colorPrimary)
            } else {
                val typedValue = android.util.TypedValue()
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
                val colorMuted = typedValue.data
                
                b.tvRankName.setTextColor(colorMuted)
                b.tvRankDays.setTextColor(colorMuted)
                b.dotRank.backgroundTintList = ColorStateList.valueOf(colorMuted)
                b.ivCheck.visibility = View.GONE
            }
            
            b.root.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
