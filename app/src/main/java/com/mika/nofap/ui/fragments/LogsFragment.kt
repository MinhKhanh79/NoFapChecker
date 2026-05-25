package com.mika.nofap.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mika.nofap.databinding.FragmentLogsBinding
import com.mika.nofap.ui.LogAdapter
import com.mika.nofap.ui.MainViewModel
import com.mika.nofap.util.Prefs

class LogsFragment : Fragment() {

    private var _b: FragmentLogsBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    private val logAdapter = LogAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentLogsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.rvLogs.layoutManager = LinearLayoutManager(requireContext())
        b.rvLogs.adapter = logAdapter
        
        setupObservers()
    }

    private fun setupObservers() {
        vm.state.observe(viewLifecycleOwner) { 
            // Refresh logs from Prefs
            val logs = Prefs.getLogs(requireContext())
                .sortedByDescending { it.totalMs } // Leaderboard: Highest ms first
            logAdapter.submitList(logs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
