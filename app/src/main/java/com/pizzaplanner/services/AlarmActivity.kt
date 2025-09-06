package com.pizzaplanner.services

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.pizzaplanner.R
import com.pizzaplanner.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAlarmBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show over lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupAlarmDisplay()
        setupClickListeners()
    }
    
    private fun setupAlarmDisplay() {
        val stepName = intent.getStringExtra("step_name") ?: "Recipe Step"
        val message = intent.getStringExtra("message") ?: "Time for next step!"
        val alarmType = intent.getStringExtra("alarm_type") ?: "STEP_START"
        
        binding.apply {
            textViewStepName.text = stepName
            textViewMessage.text = message
            
            // Customize based on alarm type
            when (alarmType) {
                "FINAL_COMPLETION" -> {
                    textViewTitle.text = getString(R.string.alarm_title_final)
                    buttonDismiss.text = getString(R.string.alarm_button_dismiss_final)
                }
                "STEP_START" -> {
                    textViewTitle.text = getString(R.string.alarm_title_step)
                    buttonDismiss.text = getString(R.string.alarm_button_dismiss)
                }
                else -> {
                    textViewTitle.text = getString(R.string.alarm_title_generic)
                    buttonDismiss.text = getString(R.string.alarm_button_dismiss_generic)
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonDismiss.setOnClickListener {
            finish()
        }
        
        binding.buttonSnooze.setOnClickListener {
            // TODO: Implement snooze functionality
            finish()
        }
    }
}
