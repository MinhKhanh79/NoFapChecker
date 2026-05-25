package com.mika.nofap

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import rikka.shizuku.ShizukuProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enforce Light Mode globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Pixel 8 Optimization: Enable Dynamic Colors (Material You)
        DynamicColors.applyToActivitiesIfAvailable(this)

        // Enable multi-process support for Shizuku
        ShizukuProvider.enableMultiProcessSupport(false)
    }
}
