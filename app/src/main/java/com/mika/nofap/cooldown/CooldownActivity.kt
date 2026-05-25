package com.mika.nofap.cooldown

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mika.nofap.R
import com.mika.nofap.databinding.ActivityCooldownBinding
import com.mika.nofap.util.HapticHelper
import kotlinx.coroutines.*

class CooldownActivity : AppCompatActivity() {

    private lateinit var b: ActivityCooldownBinding
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCooldownBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupUI()
        startCountdown()
    }

    private fun setupUI() {
        val quotes = resources.getStringArray(R.array.meditative_quotes)
        b.tvQuote.text = quotes.random()

        b.btnStayStrong.setOnClickListener {
            HapticHelper.click(it)
            setResult(RESULT_CANCELED)
            finish()
        }

        b.btnGiveUp.setOnClickListener {
            HapticHelper.heavy(this)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun startCountdown() {
        scope.launch {
            for (i in 15 downTo 0) {
                b.tvTimer.text = getString(R.string.cooldown_timer_format, i)
                if (i == 0) {
                    HapticHelper.warning(this@CooldownActivity)
                    b.btnGiveUp.isEnabled = true
                    b.btnGiveUp.setTextColor(getColor(R.color.accent_red))
                }
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
