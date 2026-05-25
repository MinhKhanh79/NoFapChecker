package com.mika.nofap.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mika.nofap.ui.fragments.LogsFragment
import com.mika.nofap.ui.fragments.RankFragment
import com.mika.nofap.ui.fragments.StreakFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StreakFragment()
            1 -> RankFragment()
            else -> LogsFragment()
        }
    }
}
