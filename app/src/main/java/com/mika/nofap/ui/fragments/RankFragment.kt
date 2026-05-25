package com.mika.nofap.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mika.nofap.R
import com.mika.nofap.databinding.FragmentRankBinding
import com.mika.nofap.ui.MainViewModel
import com.mika.nofap.ui.RankAdapter
import com.mika.nofap.ui.RankItem
import com.mika.nofap.util.Prefs
import java.util.concurrent.TimeUnit

class RankFragment : Fragment() {

    private var _b: FragmentRankBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    private val rankAdapter = RankAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentRankBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        b.rvRanks.layoutManager = LinearLayoutManager(requireContext())
        b.rvRanks.adapter = rankAdapter
    }

    private fun setupObservers() {
        vm.state.observe(viewLifecycleOwner) { s ->
            updateRankUI(s.days)
        }
    }

    private fun updateRankUI(currentDays: Long) {
        val names = arrayOf(
            getString(R.string.rank_0), getString(R.string.rank_1), getString(R.string.rank_2),
            getString(R.string.rank_3), getString(R.string.rank_4), getString(R.string.rank_5),
            getString(R.string.rank_6), getString(R.string.rank_7), getString(R.string.rank_8),
            getString(R.string.rank_9), getString(R.string.rank_10)
        )
        val icons = arrayOf("🌱", "✨", "📜", "🔮", "🧬", "☁️", "⚡", "☯️", "🌠", "🌌", "👑")
        val dayThresholds = intArrayOf(0, 1, 3, 5, 10, 15, 30, 60, 90, 180, 365)
        
        val list = names.mapIndexed { index, name ->
            RankItem(name, dayThresholds[index], currentDays >= dayThresholds[index])
        }
        rankAdapter.submitList(list)

        var currentRankIndex = 0
        for (i in dayThresholds.indices.reversed()) {
            if (currentDays >= dayThresholds[i]) {
                currentRankIndex = i
                break
            }
        }

        b.rankHeader.tvCurrentRank.text = names[currentRankIndex]
        b.rankHeader.tvRankIcon.text = icons[currentRankIndex]

        if (currentRankIndex < dayThresholds.size - 1) {
            val nextThreshold = dayThresholds[currentRankIndex + 1]
            val prevThreshold = dayThresholds[currentRankIndex]
            val totalNeeded = nextThreshold - prevThreshold
            val progress = currentDays - prevThreshold
            val percent = (progress.toFloat() / totalNeeded * 100).toInt()
            b.rankHeader.pbExp.progress = percent

            val startMs = Prefs.getStreakStartMs(requireContext())
            if (startMs != 0L) {
                val nextRankMs = startMs + (nextThreshold.toLong() * 24 * 3600 * 1000)
                val diffMs = nextRankMs - System.currentTimeMillis()
                
                if (diffMs > 0) {
                    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
                    val hours = TimeUnit.MILLISECONDS.toHours(diffMs) % 24
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs) % 60
                    b.rankHeader.tvNextBreakthrough.text = "Đột phá %s sau: %d ngày %02d:%02d:%02d".format(
                        names[currentRankIndex + 1], days, hours, minutes, seconds
                    )
                }
            }
        } else {
            b.rankHeader.pbExp.progress = 100
            b.rankHeader.tvNextBreakthrough.text = getString(R.string.rank_max)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
