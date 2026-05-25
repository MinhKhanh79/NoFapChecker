package com.mika.nofap.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticHelper {

    /**
     * Standard click vibration - optimized for Pixel's crisp haptics
     */
    fun click(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    /**
     * Success vibration (e.g., Rank Up, DNS Enabled)
     */
    fun success(context: Context) {
        vibrate(context, longArrayOf(0, 50, 100, 50), intArrayOf(0, 255, 0, 255))
    }

    /**
     * Warning/Alert vibration (e.g., Entering Cooldown)
     */
    fun warning(context: Context) {
        vibrate(context, longArrayOf(0, 100), intArrayOf(0, 150))
    }

    /**
     * Heavy reset vibration
     */
    fun heavy(context: Context) {
        vibrate(context, longArrayOf(0, 200), intArrayOf(0, 255))
    }

    /**
     * Easter Egg long vibration (2 seconds)
     */
    fun longVibrate(context: Context, durationMs: Long) {
        val vibrator = getVibrator(context)
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    @SuppressLint("MissingPermission")
    private fun vibrate(context: Context, timings: LongArray, amplitudes: IntArray) {
        val vibrator = getVibrator(context)
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }
}
