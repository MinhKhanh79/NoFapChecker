package com.mika.nofap.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class StreakLog(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val dateTime: String,
    val reason: String,
    val totalMs: Long
)

object Prefs {
    private const val NAME = "nofap_prefs"
    private const val TAG = "Prefs"

    // Keys
    private const val KEY_STREAK_START = "streak_start_ms"
    private const val KEY_DNS_ENABLED = "dns_enabled"
    private const val KEY_LONGEST_STREAK = "longest_streak_days"
    private const val KEY_TOTAL_RESETS = "total_resets"
    private const val KEY_THEME = "app_theme"
    private const val KEY_STREAK_LOGS = "streak_logs"
    private const val KEY_EASTER_EGG_ACTIVE = "easter_egg_active"
    private const val KEY_CUSTOM_DNS = "custom_dns_hostname"
    private const val KEY_DNS_PROVIDER_TYPE = "dns_provider_type"

    private fun sp(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    /* ---- settings ---- */

    fun getTheme(ctx: Context): Int = sp(ctx).getInt(KEY_THEME, 0)
    fun setTheme(ctx: Context, theme: Int) = sp(ctx).edit().putInt(KEY_THEME, theme).apply()

    fun getDnsProviderType(ctx: Context): Int = sp(ctx).getInt(KEY_DNS_PROVIDER_TYPE, 0) // 0: AdGuard, 1: CleanBrowsing, 2: Custom
    fun setDnsProviderType(ctx: Context, type: Int) = sp(ctx).edit().putInt(KEY_DNS_PROVIDER_TYPE, type).apply()

    fun getCustomDns(ctx: Context): String = sp(ctx).getString(KEY_CUSTOM_DNS, "family.adguard-dns.com") ?: "family.adguard-dns.com"
    fun setCustomDns(ctx: Context, dns: String) = sp(ctx).edit().putString(KEY_CUSTOM_DNS, dns).apply()

    fun getEffectiveDns(ctx: Context): String {
        return when (getDnsProviderType(ctx)) {
            0 -> "family.adguard-dns.com"
            1 -> "family.cleanbrowsing.org"
            else -> getCustomDns(ctx)
        }
    }

    /* ---- easter egg ---- */

    fun isEasterEggActive(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_EASTER_EGG_ACTIVE, false)
    fun setEasterEggActive(ctx: Context, active: Boolean) = sp(ctx).edit().putBoolean(KEY_EASTER_EGG_ACTIVE, active).apply()

    /* ---- streak ---- */

    fun getStreakStartMs(ctx: Context): Long = sp(ctx).getLong(KEY_STREAK_START, 0L)

    fun setStreakStartMs(ctx: Context, ms: Long) {
        Log.d(TAG, "Saving streak start: $ms")
        sp(ctx).edit().putLong(KEY_STREAK_START, ms).apply()
    }

    fun resetStreak(ctx: Context, reason: String = "Không rõ") {
        val currentStart = getStreakStartMs(ctx)
        if (currentStart != 0L) {
            val diff = System.currentTimeMillis() - currentStart
            val totalSecs = diff / 1000
            val d = totalSecs / (24 * 3600)
            val h = (totalSecs % (24 * 3600)) / 3600
            val m = (totalSecs % 3600) / 60
            
            val longest = getLongestStreak(ctx)
            if (d > longest) setLongestStreak(ctx, d)
            incrementResets(ctx)
            
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val newLog = StreakLog(d, h, m, dateStr, reason, diff)
            saveLog(ctx, newLog)
        }
        setStreakStartMs(ctx, 0L)
        setDnsEnabled(ctx, false)
        setEasterEggActive(ctx, false)
    }

    private fun saveLog(ctx: Context, log: StreakLog) {
        val currentLogs = getLogs(ctx).toMutableList()
        currentLogs.add(log)
        val json = Gson().toJson(currentLogs)
        sp(ctx).edit().putString(KEY_STREAK_LOGS, json).apply()
    }

    fun getLogs(ctx: Context): List<StreakLog> {
        val json = sp(ctx).getString(KEY_STREAK_LOGS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<StreakLog>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun startStreak(ctx: Context) {
        if (getStreakStartMs(ctx) == 0L) {
            setStreakStartMs(ctx, System.currentTimeMillis())
        }
        setEasterEggActive(ctx, false)
    }

    fun getStreakDays(ctx: Context): Long {
        val start = getStreakStartMs(ctx)
        if (start == 0L) return 0L
        return (System.currentTimeMillis() - start) / (1000 * 60 * 60 * 24)
    }

    /* ---- dns ---- */

    fun isDnsEnabled(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_DNS_ENABLED, false)

    fun setDnsEnabled(ctx: Context, enabled: Boolean) =
        sp(ctx).edit().putBoolean(KEY_DNS_ENABLED, enabled).apply()

    /* ---- stats ---- */

    fun getLongestStreak(ctx: Context): Long = sp(ctx).getLong(KEY_LONGEST_STREAK, 0L)

    fun setLongestStreak(ctx: Context, days: Long) =
        sp(ctx).edit().putLong(KEY_LONGEST_STREAK, days).apply()

    fun getTotalResets(ctx: Context): Int = sp(ctx).getInt(KEY_TOTAL_RESETS, 0)

    private fun incrementResets(ctx: Context) {
        val cur = getTotalResets(ctx)
        sp(ctx).edit().putInt(KEY_TOTAL_RESETS, cur + 1).apply()
    }
}
