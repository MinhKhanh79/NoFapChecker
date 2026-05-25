package com.mika.nofap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mika.nofap.R
import com.mika.nofap.util.DnsHelper
import com.mika.nofap.util.Prefs
import kotlinx.coroutines.*

data class StreakState(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isActive: Boolean = false,
    val dnsEnabled: Boolean = false,
    val systemDnsActive: Boolean = false,
    val longestStreak: Long = 0,
    val totalResets: Int = 0,
    val shizukuAvailable: Boolean = false
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val state = MutableLiveData(StreakState())
    val toastMsg = MutableLiveData<Int?>()

    private var tickerJob: Job? = null

    init {
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                refreshState()
                delay(1000)
            }
        }
    }

    fun refreshState() {
        val ctx = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val effectiveDns = Prefs.getEffectiveDns(ctx)
            val actualDnsActive = DnsHelper.isAdultFilterActive(effectiveDns)
            val startMs = Prefs.getStreakStartMs(ctx)
            val prefsDnsEnabled = Prefs.isDnsEnabled(ctx)
            val easterEggActive = Prefs.isEasterEggActive(ctx)

            if (startMs != 0L && !easterEggActive) {
                if (!actualDnsActive && prefsDnsEnabled) {
                    withContext(Dispatchers.Main) {
                        Prefs.resetStreak(ctx, "Tự động phát hiện DNS bị tắt")
                    }
                }
            }

            var d = 0L
            var h = 0L
            var m = 0L
            var s = 0L
            
            val currentStartMs = Prefs.getStreakStartMs(ctx)
            if (currentStartMs != 0L) {
                val diff = System.currentTimeMillis() - currentStartMs
                val totalSecs = diff / 1000
                d = totalSecs / (24 * 3600)
                h = (totalSecs % (24 * 3600)) / 3600
                m = (totalSecs % 3600) / 60
                s = totalSecs % 60
            }

            withContext(Dispatchers.Main) {
                state.value = StreakState(
                    days = d,
                    hours = h,
                    minutes = m,
                    seconds = s,
                    isActive = currentStartMs != 0L,
                    dnsEnabled = prefsDnsEnabled, 
                    systemDnsActive = actualDnsActive,
                    longestStreak = Prefs.getLongestStreak(ctx),
                    totalResets = Prefs.getTotalResets(ctx),
                    shizukuAvailable = DnsHelper.isShizukuAvailable()
                )
            }
        }
    }

    fun toggleDns(enable: Boolean, reason: String = "Không rõ") {
        val ctx = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            Prefs.setEasterEggActive(ctx, false)
            val effectiveDns = Prefs.getEffectiveDns(ctx)
            
            if (enable) {
                val ok = DnsHelper.enableAdultFilter(effectiveDns)
                if (ok) {
                    Prefs.setDnsEnabled(ctx, true)
                    Prefs.startStreak(ctx)
                    withContext(Dispatchers.Main) { toastMsg.value = R.string.toast_dns_enabled }
                } else {
                    withContext(Dispatchers.Main) { toastMsg.value = R.string.toast_dns_enable_failed }
                }
            } else {
                val ok = DnsHelper.disableAdultFilter()
                if (ok) {
                    Prefs.setDnsEnabled(ctx, false)
                    Prefs.resetStreak(ctx, reason)
                    withContext(Dispatchers.Main) { toastMsg.value = R.string.toast_dns_disabled }
                } else {
                    withContext(Dispatchers.Main) { toastMsg.value = R.string.toast_dns_disable_failed }
                }
            }
            refreshState()
        }
    }

    fun requestShizukuPermission(requestCode: Int) {
        DnsHelper.requestShizukuPermission(requestCode)
    }
}
