package net.broodjeaap.pizzaplanner2.services

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import net.broodjeaap.pizzaplanner2.MainActivity
import net.broodjeaap.pizzaplanner2.R
import net.broodjeaap.pizzaplanner2.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var volumeIncreaseRunnable: Runnable? = null
    private var currentVolume = 0.1f
    private val volumeIncreaseStep = 0.1f
    private val volumeIncreaseDelay = 2000L // 2 seconds
    private lateinit var sharedPreferences: android.content.SharedPreferences
    
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
        initializeSettings()
        startAlarm()
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
    
    private fun initializeSettings() {
        sharedPreferences = getSharedPreferences("pizza_planner_settings", Context.MODE_PRIVATE)
    }
    
    private fun startAlarm() {
        // Vibrate if enabled
        if (sharedPreferences.getBoolean("vibration_enabled", true)) {
            vibrate()
        }
        
        // Play alarm sound
        playAlarmSound()
        
        // Start volume increase if enabled
        if (sharedPreferences.getBoolean("increasing_volume", true)) {
            startVolumeIncrease()
        }
    }
    
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationPattern = longArrayOf(0, 500, 200, 500)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, 0)
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationPattern, 0)
        }
    }
    
    private fun playAlarmSound() {
        try {
            // Get the alarm sound URI from settings
            val uriString = sharedPreferences.getString("alarm_sound_uri", null)
            val alarmSoundUri = if (uriString != null) {
                Uri.parse(uriString)
            } else {
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            }
            
            // Create MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@AlarmActivity, alarmSoundUri)
                prepareAsync()
                
                // Set initial volume if increasing volume is enabled
                if (sharedPreferences.getBoolean("increasing_volume", true)) {
                    setVolume(currentVolume, currentVolume)
                }
                
                setOnPreparedListener {
                    // Start playing
                    start()
                    
                    // Set looping if repeat is enabled
                    isLooping = sharedPreferences.getBoolean("alarm_repeat", true)
                }
                
                setOnCompletionListener {
                    // If repeat is enabled and not already looping, play again
                    if (sharedPreferences.getBoolean("alarm_repeat", true) && !isLooping) {
                        seekTo(0)
                        start()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startVolumeIncrease() {
        volumeIncreaseRunnable = object : Runnable {
            override fun run() {
                currentVolume = (currentVolume + volumeIncreaseStep).coerceAtMost(1.0f)
                mediaPlayer?.setVolume(currentVolume, currentVolume)
                
                // Continue increasing volume until it reaches maximum
                if (currentVolume < 1.0f) {
                    handler.postDelayed(this, volumeIncreaseDelay)
                }
            }
        }
        
        handler.post(volumeIncreaseRunnable!!)
    }
    
    private fun setupClickListeners() {
        binding.buttonDismiss.setOnClickListener {
            stopAlarm()
            launchMainActivity()
            finish()
        }
        
        binding.buttonSnooze.setOnClickListener {
            stopAlarm()
            // TODO: Implement snooze functionality
            finish()
        }
    }
    
    private fun launchMainActivity() {
        val recipeId = intent.getStringExtra("recipe_id") ?: return
        val stepId = intent.getStringExtra("step_id") ?: return
        
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("recipe_id", recipeId)
            putExtra("step_id", stepId)
        }
        startActivity(mainIntent)
    }
    
    private fun stopAlarm() {
        // Stop media player
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            mediaPlayer = null
        }
        
        // Stop volume increase handler
        volumeIncreaseRunnable?.let {
            handler.removeCallbacks(it)
            volumeIncreaseRunnable = null
        }
        
        // Stop vibration
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
