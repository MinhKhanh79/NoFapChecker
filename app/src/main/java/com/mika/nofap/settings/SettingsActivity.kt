package com.mika.nofap.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mika.nofap.R
import com.mika.nofap.databinding.ActivitySettingsBinding
import com.mika.nofap.util.DnsHelper
import com.mika.nofap.util.HapticHelper
import com.mika.nofap.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding
    private var versionTapCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        b.toolbar.setNavigationOnClickListener { finish() }

        setupUI()
    }

    private fun setupUI() {
        // Version
        b.tvVersion.text = "1.0.0"
        b.btnVersion.setOnClickListener {
            handleVersionTap()
        }

        // DNS Provider Dropdown
        updateProviderText()
        b.btnDnsProvider.setOnClickListener {
            HapticHelper.click(it)
            val options = arrayOf(
                getString(R.string.dns_provider_adguard),
                getString(R.string.dns_provider_cleanbrowsing),
                getString(R.string.dns_provider_custom)
            )
            AlertDialog.Builder(this)
                .setTitle(R.string.label_provider)
                .setItems(options) { _, which ->
                    Prefs.setDnsProviderType(this, which)
                    updateProviderText()
                    updateDnsVisibility()
                }.show()
        }

        // Custom DNS Input
        updateDnsText()
        updateDnsVisibility()
        b.btnCustomDns.setOnClickListener {
            HapticHelper.click(it)
            showDnsInputDialog()
        }
    }

    private fun updateProviderText() {
        val type = Prefs.getDnsProviderType(this)
        b.tvCurrentProvider.text = when(type) {
            0 -> getString(R.string.dns_provider_adguard)
            1 -> getString(R.string.dns_provider_cleanbrowsing)
            else -> getString(R.string.dns_provider_custom)
        }
    }

    private fun updateDnsVisibility() {
        val isCustom = Prefs.getDnsProviderType(this) == 2
        val visibility = if (isCustom) View.VISIBLE else View.GONE
        b.btnCustomDns.visibility = visibility
        b.dividerCustomDns.visibility = visibility
    }

    private fun showDnsInputDialog() {
        val input = android.widget.EditText(this)
        input.setText(Prefs.getCustomDns(this))
        input.hint = "ví dụ: family.adguard-dns.com"
        
        AlertDialog.Builder(this)
            .setTitle(R.string.label_custom_dns_field)
            .setView(input)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val newDns = input.text.toString().trim()
                if (newDns.isNotEmpty()) {
                    Prefs.setCustomDns(this, newDns)
                    updateDnsText()
                    Toast.makeText(this, R.string.toast_dns_updated, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun updateDnsText() {
        b.tvCurrentDns.text = Prefs.getCustomDns(this)
    }

    private fun handleVersionTap() {
        versionTapCount++
        if (versionTapCount == 8) {
            triggerEasterEgg()
            versionTapCount = 0
        } else if (versionTapCount > 4) {
            Toast.makeText(this, "Còn ${8 - versionTapCount} bước nữa...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerEasterEgg() {
        HapticHelper.longVibrate(this, 2000)
        Prefs.setEasterEggActive(this, true)
        Prefs.setDnsEnabled(this, false)
        
        lifecycleScope.launch(Dispatchers.IO) {
            DnsHelper.disableAdultFilter()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@SettingsActivity, R.string.easter_egg_reward, Toast.LENGTH_LONG).show()
                Toast.makeText(this@SettingsActivity, R.string.easter_egg_toast, Toast.LENGTH_SHORT).show()
                openChromeIncognito()
            }
        }
    }

    private fun openChromeIncognito() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            intent.setPackage("com.android.chrome")
            intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setClassName("com.android.chrome", "com.google.android.apps.chrome.Main")
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                intent.setPackage("com.android.chrome")
                intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                startActivity(intent)
            }
        }
    }
}
