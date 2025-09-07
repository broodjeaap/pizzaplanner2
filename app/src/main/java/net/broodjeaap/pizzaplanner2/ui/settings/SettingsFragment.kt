package net.broodjeaap.pizzaplanner2.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.broodjeaap.pizzaplanner2.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sharedPreferences: SharedPreferences
    
    // Settings keys
    companion object {
        private const val PREFS_NAME = "pizza_planner_settings"
        private const val KEY_ALARM_SOUND_URI = "alarm_sound_uri"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_NOTIFICATION_STYLE = "notification_style"
        private const val KEY_AUTO_UPDATE_RECIPES = "auto_update_recipes"
        private const val KEY_DEFAULT_RISE_TIME = "default_rise_time"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_ALARM_REPEAT = "alarm_repeat"
        private const val KEY_INCREASING_VOLUME = "increasing_volume"
        
        // Default values
        private const val DEFAULT_RISE_TIME = 24.0 // hours
        private const val DEFAULT_VIBRATION = true
        private const val DEFAULT_AUTO_UPDATE = true
        private const val DEFAULT_KEEP_SCREEN_ON = false
        private const val DEFAULT_NOTIFICATION_STYLE = "Full Screen Alarm"
        private const val DEFAULT_ALARM_REPEAT = true
        private const val DEFAULT_INCREASING_VOLUME = true
    }
    
    // Activity result launcher for ringtone picker
    private val ringtonePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                saveAlarmSoundUri(uri)
                updateAlarmSoundDisplay(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeSharedPreferences()
        setupClickListeners()
        loadSettings()
    }
    
    private fun initializeSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun setupClickListeners() {
        // Alarm sound selection
        binding.buttonSelectAlarmSound.setOnClickListener {
            openRingtonePicker()
        }
        
        // Vibration toggle
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            saveVibrationSetting(isChecked)
        }
        
        // Notification style selection
        binding.buttonSelectNotificationStyle.setOnClickListener {
            showNotificationStyleDialog()
        }
        
        // Auto-update recipes toggle
        binding.switchAutoUpdate.setOnCheckedChangeListener { _, isChecked ->
            saveAutoUpdateSetting(isChecked)
        }
        
        // Download recipes button
        binding.buttonDownloadRecipes.setOnClickListener {
            checkForRecipeUpdates()
        }
        
        // Default rise time setting
        binding.buttonSetDefaultRiseTime.setOnClickListener {
            showDefaultRiseTimeDialog()
        }
        
        // Keep screen on toggle
        binding.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            saveKeepScreenOnSetting(isChecked)
        }
        
        // Alarm repeat toggle
        binding.switchAlarmRepeat.setOnCheckedChangeListener { _, isChecked ->
            saveAlarmRepeatSetting(isChecked)
        }
        
        // Increasing volume toggle
        binding.switchIncreasingVolume.setOnCheckedChangeListener { _, isChecked ->
            saveIncreasingVolumeSetting(isChecked)
        }
        
        // Reset settings button
        binding.buttonResetSettings.setOnClickListener {
            showResetSettingsDialog()
        }
    }
    
    private fun loadSettings() {
        // Load alarm sound
        val alarmSoundUri = getAlarmSoundUri()
        updateAlarmSoundDisplay(alarmSoundUri)
        
        // Load vibration setting
        val vibrationEnabled = sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION)
        binding.switchVibration.isChecked = vibrationEnabled
        
        // Load notification style
        val notificationStyle = sharedPreferences.getString(KEY_NOTIFICATION_STYLE, DEFAULT_NOTIFICATION_STYLE)
        binding.textViewNotificationStyleValue.text = notificationStyle
        
        // Load auto-update setting
        val autoUpdate = sharedPreferences.getBoolean(KEY_AUTO_UPDATE_RECIPES, DEFAULT_AUTO_UPDATE)
        binding.switchAutoUpdate.isChecked = autoUpdate
        
        // Load default rise time
        val defaultRiseTime = sharedPreferences.getFloat(KEY_DEFAULT_RISE_TIME, DEFAULT_RISE_TIME.toFloat())
        binding.textViewDefaultRiseTimeValue.text = "${defaultRiseTime.toInt()} hours"
        
        // Load keep screen on setting
        val keepScreenOn = sharedPreferences.getBoolean(KEY_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON)
        binding.switchKeepScreenOn.isChecked = keepScreenOn
        
        // Load alarm repeat setting
        val alarmRepeat = sharedPreferences.getBoolean(KEY_ALARM_REPEAT, DEFAULT_ALARM_REPEAT)
        binding.switchAlarmRepeat.isChecked = alarmRepeat
        
        // Load increasing volume setting
        val increasingVolume = sharedPreferences.getBoolean(KEY_INCREASING_VOLUME, DEFAULT_INCREASING_VOLUME)
        binding.switchIncreasingVolume.isChecked = increasingVolume
    }
    
    private fun openRingtonePicker() {
        val intent = android.content.Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getAlarmSoundUri())
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        }
        ringtonePickerLauncher.launch(intent)
    }
    
    private fun saveAlarmSoundUri(uri: Uri) {
        sharedPreferences.edit()
            .putString(KEY_ALARM_SOUND_URI, uri.toString())
            .apply()
    }
    
    private fun getAlarmSoundUri(): Uri? {
        val uriString = sharedPreferences.getString(KEY_ALARM_SOUND_URI, null)
        return if (uriString != null) {
            Uri.parse(uriString)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
    }
    
    private fun updateAlarmSoundDisplay(uri: Uri?) {
        val ringtone = RingtoneManager.getRingtone(requireContext(), uri)
        val title = ringtone?.getTitle(requireContext()) ?: "Default"
        binding.textViewAlarmSoundValue.text = title
    }
    
    private fun saveVibrationSetting(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_VIBRATION_ENABLED, enabled)
            .apply()
    }
    
    private fun showNotificationStyleDialog() {
        val options = arrayOf("Full Screen Alarm", "Notification Only", "Silent")
        val currentStyle = sharedPreferences.getString(KEY_NOTIFICATION_STYLE, DEFAULT_NOTIFICATION_STYLE)
        val currentIndex = options.indexOf(currentStyle)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notification Style")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val selectedStyle = options[which]
                saveNotificationStyle(selectedStyle)
                binding.textViewNotificationStyleValue.text = selectedStyle
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun saveNotificationStyle(style: String) {
        sharedPreferences.edit()
            .putString(KEY_NOTIFICATION_STYLE, style)
            .apply()
    }
    
    private fun saveAutoUpdateSetting(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_AUTO_UPDATE_RECIPES, enabled)
            .apply()
    }
    
    private fun checkForRecipeUpdates() {
        // Simulate checking for recipe updates
        binding.buttonDownloadRecipes.isEnabled = false
        binding.buttonDownloadRecipes.text = "Checking..."
        
        lifecycleScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(2000)
            
            binding.buttonDownloadRecipes.isEnabled = true
            binding.buttonDownloadRecipes.text = "Check Now"
            
            // Show result
            Toast.makeText(requireContext(), "No new recipes available", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showDefaultRiseTimeDialog() {
        val currentRiseTime = sharedPreferences.getFloat(KEY_DEFAULT_RISE_TIME, DEFAULT_RISE_TIME.toFloat())
        val options = arrayOf("12 hours", "18 hours", "24 hours", "36 hours", "48 hours")
        val values = arrayOf(12f, 18f, 24f, 36f, 48f)
        val currentIndex = values.indexOf(currentRiseTime).takeIf { it >= 0 } ?: 2 // Default to 24 hours
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default Rise Time")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val selectedValue = values[which]
                saveDefaultRiseTime(selectedValue)
                binding.textViewDefaultRiseTimeValue.text = "${selectedValue.toInt()} hours"
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun saveDefaultRiseTime(hours: Float) {
        sharedPreferences.edit()
            .putFloat(KEY_DEFAULT_RISE_TIME, hours)
            .apply()
    }
    
    private fun saveKeepScreenOnSetting(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_KEEP_SCREEN_ON, enabled)
            .apply()
    }
    
    private fun saveAlarmRepeatSetting(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_ALARM_REPEAT, enabled)
            .apply()
    }
    
    private fun saveIncreasingVolumeSetting(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_INCREASING_VOLUME, enabled)
            .apply()
    }
    
    private fun showResetSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset All Settings")
            .setMessage("Are you sure you want to reset all settings to their default values? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                resetAllSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetAllSettings() {
        sharedPreferences.edit().clear().apply()
        loadSettings()
        Toast.makeText(requireContext(), "Settings reset to defaults", Toast.LENGTH_SHORT).show()
    }
    
    // Public methods for other parts of the app to access settings
    
    // Public methods for other parts of the app to access settings
    fun getVibrationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION)
    }
    
    fun getNotificationStyle(): String {
        return sharedPreferences.getString(KEY_NOTIFICATION_STYLE, DEFAULT_NOTIFICATION_STYLE) ?: DEFAULT_NOTIFICATION_STYLE
    }
    
    fun getAutoUpdateEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_UPDATE_RECIPES, DEFAULT_AUTO_UPDATE)
    }
    
    fun getDefaultRiseTime(): Float {
        return sharedPreferences.getFloat(KEY_DEFAULT_RISE_TIME, DEFAULT_RISE_TIME.toFloat())
    }
    
    fun getKeepScreenOnEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON)
    }
    
    fun getAlarmRepeatEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_ALARM_REPEAT, DEFAULT_ALARM_REPEAT)
    }
    
    fun getIncreasingVolumeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_INCREASING_VOLUME, DEFAULT_INCREASING_VOLUME)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
