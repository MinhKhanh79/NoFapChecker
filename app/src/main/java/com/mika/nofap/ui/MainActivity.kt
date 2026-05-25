package com.mika.nofap.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.tabs.TabLayoutMediator
import com.mika.nofap.R
import com.mika.nofap.databinding.ActivityMainBinding
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    private val shizukuListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        vm.refreshState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(b.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        setupViewPager()
        Shizuku.addRequestPermissionResultListener(shizukuListener)
        vm.refreshState()
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        b.viewPager.adapter = adapter
        b.viewPager.offscreenPageLimit = 2

        TabLayoutMediator(b.tabLayout, b.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_streak)
                1 -> getString(R.string.tab_rank)
                else -> getString(R.string.tab_logs)
            }
        }.attach()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuListener)
        super.onDestroy()
    }
}
