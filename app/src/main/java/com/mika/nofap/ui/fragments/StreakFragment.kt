package com.mika.nofap.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mika.nofap.R
import com.mika.nofap.cooldown.CooldownActivity
import com.mika.nofap.databinding.FragmentStreakBinding
import com.mika.nofap.settings.SettingsActivity
import com.mika.nofap.ui.MainViewModel
import com.mika.nofap.util.DnsHelper
import com.mika.nofap.util.HapticHelper
import com.mika.nofap.util.Prefs
import rikka.shizuku.Shizuku
import java.util.Calendar

class StreakFragment : Fragment() {

    private var _b: FragmentStreakBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    private val shizukuCode = 1001

    private val cooldownLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            showResetDialog()
        } else {
            vm.refreshState()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentStreakBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        setupGestures()
        startFlameAnimation()
    }

    private fun startFlameAnimation() {
        val pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.flame_pulse)
        b.ivFlame.startAnimation(pulse)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                showManualStreakPicker()
                return true
            }
        })
        b.cardClock.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun showManualStreakPicker() {
        val calendar = Calendar.getInstance()
        val currentStart = Prefs.getStreakStartMs(requireContext())
        if (currentStart != 0L) {
            calendar.timeInMillis = currentStart
        }

        // 1. Pick Date
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // 2. Pick Time
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.setTitle(getString(R.string.dialog_streak_title))
        datePicker.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val selectedMs = calendar.timeInMillis
                if (selectedMs <= System.currentTimeMillis()) {
                    Prefs.setStreakStartMs(requireContext(), selectedMs)
                    HapticHelper.success(requireContext())
                    vm.refreshState()
                    Toast.makeText(requireContext(), R.string.toast_streak_updated, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Ngày khởi đầu không thể ở tương lai!", Toast.LENGTH_SHORT).show()
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun setupObservers() {
        vm.state.observe(viewLifecycleOwner) { s ->
            b.tvDays.text = s.days.toString()
            b.tvHours.text = getString(R.string.time_format_hours, s.hours)
            b.tvMinutes.text = getString(R.string.time_format_minutes, s.minutes)
            b.tvSeconds.text = "%02d".format(s.seconds)

            val dotColor = if (s.systemDnsActive) R.color.accent_green else R.color.accent_red
            val glowColor = if (s.systemDnsActive) "#402ECC71" else "#40FF4757"
            
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_status_dot_glow) as? LayerDrawable
            drawable?.let { layer ->
                val outer = layer.getDrawable(0) as? GradientDrawable
                outer?.setColor(Color.parseColor(glowColor))
                val inner = layer.getDrawable(1) as? GradientDrawable
                inner?.setColor(ContextCompat.getColor(requireContext(), dotColor))
                b.viewStatusDot.background = layer
            }

            b.switchDns.setOnCheckedChangeListener(null)
            b.switchDns.isChecked = s.dnsEnabled
            b.switchDns.setOnCheckedChangeListener { _, checked -> 
                HapticHelper.click(b.switchDns)
                if (!checked) {
                    HapticHelper.warning(requireContext())
                    cooldownLauncher.launch(Intent(requireContext(), CooldownActivity::class.java))
                } else {
                    vm.toggleDns(true)
                }
            }

            b.tvStatus.text = when {
                !s.shizukuAvailable -> getString(R.string.status_shizuku_not_ready)
                s.dnsEnabled && s.isActive -> getString(R.string.status_protecting)
                else -> getString(R.string.status_not_started)
            }

            b.tvLongest.text = getString(R.string.stats_days_format, s.longestStreak)
            b.tvResets.text = getString(R.string.stats_resets_format, s.totalResets)

            // DNS Desc
            b.tvDnsDesc.text = getString(R.string.dns_desc, Prefs.getEffectiveDns(requireContext()))

            b.btnShizuku.text = if (s.shizukuAvailable) getString(R.string.shizuku_ok) else getString(R.string.shizuku_grant_permission)
            b.btnShizuku.isEnabled = !s.shizukuAvailable
        }
    }

    private fun showResetDialog() {
        val input = EditText(requireContext())
        input.hint = getString(R.string.hint_reset_reason)
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_reset_title)
            .setMessage(R.string.dialog_reset_msg)
            .setView(input)
            .setPositiveButton(R.string.btn_confirm_reset) { _, _ ->
                val reason = input.text.toString().ifBlank { "Không có lý do" }
                HapticHelper.heavy(requireContext())
                vm.toggleDns(false, reason)
                Toast.makeText(requireContext(), R.string.toast_reset_saved, Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->
                HapticHelper.click(b.root)
                vm.refreshState()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupClickListeners() {
        b.btnSettings.setOnClickListener {
            HapticHelper.click(it)
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        b.btnShizuku.setOnClickListener {
            HapticHelper.click(it)
            if (!DnsHelper.isShizukuAvailable()) {
                try {
                    vm.requestShizukuPermission(shizukuCode)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Shizuku not running or manager not found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
