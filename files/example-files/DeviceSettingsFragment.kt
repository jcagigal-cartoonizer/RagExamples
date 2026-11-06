package ifac.td.taxi.ui.screen

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.lifecycleScope
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentDeviceSettingsBinding
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.slider.CustomSettingsSlider
import ifac.td.taxi.ui.custom.slider.CustomSettingsSlider.SliderType
import ifac.td.taxi.viewmodel.DeviceSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class DeviceSettingsFragment :
    BaseFragment<FragmentDeviceSettingsBinding, DeviceSettingsViewModel>(R.layout.fragment_device_settings) {

    private val TAG = "DeviceSettingsFragment"
    private val vModel: DeviceSettingsViewModel by viewModel()

    private var minPhoneCallVolume: Int? = null

    override fun getViewModel() = vModel
    override fun getViewBinding() = FragmentDeviceSettingsBinding.inflate(layoutInflater)

    companion object {
        private const val PREF_PHONE_CALL_VOLUME = "phoneCallVolume"
        private const val PREF_SYSTEM_VOLUME = "systemVolume"
        private const val PREF_NOTIFICATION_VOLUME = "notificationVolume"
        private const val PREF_BRIGHTNESS = "brightness"
        private const val PREF_RINGTONE_VOLUME = "ringtoneVolume"
        private const val MIN_RINGTONE_VOLUME_PERCENT = 20
    }

    override fun setupComponents() {
        iMainActivity.showHeader(true)
        vBinding.ccsBrightness.setMaxProgress(255)
    }

    override fun onResume() {
        super.onResume()

        if (!Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context?.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context?.startActivity(intent)
        }

        refreshAllSliders()
        vModel.checkLinkedVolumes()
        setUpValues()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (view == null) {
            return
        }
        savedInstanceState?.apply {
            setUpPhoneCallVolumeControls(getInt(PREF_PHONE_CALL_VOLUME, getPhoneCallVolume()))
            setUpSystemVolumeControls(getInt(PREF_SYSTEM_VOLUME, getSystemVolume()))
            setUpNotificationVolumeControls(getInt(PREF_NOTIFICATION_VOLUME, getNotificationVolume()))
            setUpBrightnessControls(getInt(PREF_BRIGHTNESS, getBrightness()))
            setUpRingtoneVolumeControls(getInt(PREF_RINGTONE_VOLUME, getRingtoneVolume()))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view == null) {
            return
        }
        vBinding.apply {
            outState.putInt(PREF_PHONE_CALL_VOLUME, ccsPhoneCallVolume.getProgress())
            outState.putInt(PREF_SYSTEM_VOLUME, ccsSystemVolume.getProgress())
            outState.putInt(PREF_NOTIFICATION_VOLUME, ccsMessagesVolume.getProgress())
            outState.putInt(PREF_BRIGHTNESS, ccsBrightness.getProgress())
            outState.putInt(PREF_RINGTONE_VOLUME, ccsRingtoneVolume.getProgress())
        }
    }

    private fun setUpValues() {
        vBinding.apply {
            val phoneCallVolume = getPhoneCallVolume()
            val brightness = getBrightness()

            // 1º
            setUpSystemVolumeControls(getSystemVolume())
            setUpNotificationVolumeControls(getNotificationVolume())
            setUpRingtoneVolumeControls(getRingtoneVolume())

            // 2º
            setUpPhoneCallVolumeControls(phoneCallVolume)

            // 3º
            setUpBrightnessControls(brightness)

            // 4º
            //setUpDarkThemeControls()
        }
    }

    private fun setUpDarkThemeControls() {
        vBinding.apply {
            ccsTheme.apply {
                sliderType = SliderType.THEME
                setTitle(getString(R.string.dark_theme_title))
                setLeftIconClickCallback {}
                setRightIconClickCallback {}

                getNightMode()
                val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                Logs.d(TAG, "setUpDarkThemeControls: darkMode=$currentNightMode")

                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        setCustomSwitchValue(false)
                        setMainSwitchValue(false)
                    }

                    Configuration.UI_MODE_NIGHT_YES -> {
                        setCustomSwitchValue(false)
                        setMainSwitchValue(true)
                    }

                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        setCustomSwitchValue(false)
                        setMainSwitchValue(false)
                    }
                }

                setSwitchCustomSliderCallback { isChecked ->
                    // vModel.setAutoMode()
                }

                setSwitchMainCustomSliderCallback { isChecked ->
                    // vModel.toggleTheme(isChecked)
                    setCustomSwitchValue(false)
                }
            }
        }
    }

    private fun getNightMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {}
            Configuration.UI_MODE_NIGHT_YES -> {}
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun setUpPhoneCallVolumeControls(phoneCallVolume: Int) {
        vBinding.ccsPhoneCallVolume.apply {
            setTitle(getString(R.string.phone_call_volume_title))
            setDescription(context.getString(R.string.phone_call_volume_description))
            setProgress(phoneCallVolume)
            sliderType = SliderType.VOLUME

            if (isDoNotDisturbModeEnabled() || isSilentModeEnabled() || !vModel.hasAudioPermissions()) {
                setEnabledOrDisabled(false) {
                    iMainActivity.showToast(R.string.dnd_or_silent_mode_enabled)
                    checkSystemWriteSettings()
                }
            } else {
                setProgressChangedCustomCallback { progress, fromUser ->
                    if (fromUser) {
                        setPhoneCallVolume(progress)
                    }
                }

                setOnStopTrackingTouchCustomCallback { progress ->
                    if (!vModel.hasAudioPermissions()) {
                        Logs.e(TAG, "setPhoneCallVolume: WRITE_SETTINGS permission not granted or context null")
                        iMainActivity.showToast(R.string.error_permission_denied)
                        checkSystemWriteSettings()
                    }
                }
            }
        }
    }

    private fun setUpNotificationVolumeControls(notificationVolume: Int) {
        vBinding.ccsMessagesVolume.apply {
            setTitle(getString(R.string.notification_volume_title))
            setDescription(context.getString(R.string.notification_volume_description))
            setProgress(notificationVolume)
            sliderType = SliderType.VOLUME

            if (isDoNotDisturbModeEnabled() || isSilentModeEnabled() || !vModel.hasAudioPermissions()) {
                setEnabledOrDisabled(false) {
                    iMainActivity.showToast(R.string.dnd_or_silent_mode_enabled)
                    checkSystemWriteSettings()
                }
            } else {
                setProgressChangedCustomCallback { progress, fromUser ->
                    if (fromUser) {
                        setNotificationVolume(progress)
                    }
                }

                setOnStopTrackingTouchCustomCallback { progress ->
                    if (!vModel.hasAudioPermissions()) {
                        Logs.e(TAG, "setNotificationVolume: WRITE_SETTINGS permission not granted or context null")
                        iMainActivity.showToast(R.string.error_permission_denied)
                        checkSystemWriteSettings()
                    }

                    if ((isDoNotDisturbModeEnabled() || isSilentModeEnabled())) {
                        Logs.d(TAG, "setNotificationVolume: Do Not Disturb or silent mode enabled, volume change may be ignored")
                        iMainActivity.showToast(R.string.dnd_or_silent_mode_enabled)
                    }

                    lifecycleScope.launch(Dispatchers.Main) {
                        playBeepForStream(AudioManager.STREAM_NOTIFICATION)
                    }
                }
            }
        }
    }

    private fun setUpRingtoneVolumeControls(ringtoneVolume: Int) {
        vBinding.ccsRingtoneVolume.apply {
            setTitle(getString(R.string.ringtone_volume_title))
            setDescription(context.getString(R.string.ringtone_volume_description))
            setProgress(ringtoneVolume)
            sliderType = SliderType.VOLUME

            if (isDoNotDisturbModeEnabled() || isSilentModeEnabled() || !vModel.hasAudioPermissions()) {
                setEnabledOrDisabled(false) {
                    iMainActivity.showToast(R.string.dnd_or_silent_mode_enabled)
                    checkSystemWriteSettings()
                }
            } else {
                setProgressChangedCustomCallback { progress, fromUser ->
                    if (fromUser) {
                        setRingtoneVolume(progress)
                    }
                }

                setOnStopTrackingTouchCustomCallback { progress ->
                    if (!vModel.hasAudioPermissions()) {
                        Logs.e(TAG, "setRingtoneVolume: WRITE_SETTINGS permission not granted or context null")
                        iMainActivity.showToast(R.string.error_permission_denied)
                        checkSystemWriteSettings()
                    }
                    if (isDoNotDisturbModeEnabled()) {
                        Logs.d(TAG, "setRingtoneVolume: Do Not Disturb is enabled, volume change may be ignored")
                        iMainActivity.showToast(R.string.dnd_or_silent_mode_enabled)
                    }

                    vModel.testRingTone()

                }
            }
        }
    }

    suspend fun playBeepForStream(streamType: Int) {
        withContext(Dispatchers.Main) {
            try {
                Logs.d(TAG, "playBeepForStream: streamType: $streamType")
                val tone = ToneGenerator(streamType, ToneGenerator.MAX_VOLUME)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP)
                withContext(Dispatchers.Default) {
                    delay(300)
                    tone.stopTone()
                    tone.release()
                }
            } catch (e: Exception) {
                Logs.e(TAG, "playBeepForStream: $e")
            }
        }
    }

    private fun setUpSystemVolumeControls(systemVolume: Int) {
        vBinding.ccsSystemVolume.apply {
            setTitle(getString(R.string.system_volume_title))
            setDescription(context.getString(R.string.system_volume_description))
            setProgress(systemVolume)
            sliderType = SliderType.VOLUME

            setProgressChangedCustomCallback { progress, fromUser ->
                if (fromUser) {
                    setSystemVolume(progress)
                }
            }

            setOnStopTrackingTouchCustomCallback { progress ->
                if (!vModel.hasAudioPermissions()) {
                    Logs.e(TAG, "setSystemVolume: WRITE_SETTINGS permission not granted or context null")
                    iMainActivity.showToast(R.string.error_permission_denied)
                    checkSystemWriteSettings()
                }
                if (isDoNotDisturbModeEnabled()) {
                    Logs.d(TAG, "setSystemVolume: Do Not Disturb is enabled, volume change may be ignored")
                    iMainActivity.showToast(R.string.dnd_or_silent_mode_enabled)
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    playBeepForStream(AudioManager.STREAM_MUSIC)
                }
            }
        }
    }

    private fun setUpBrightnessControls(brightness: Int) {
        vBinding.ccsBrightness.apply {
            setTitle(getString(R.string.brightness_title))
            setDescription(context.getString(R.string.brightness_description))
            setProgress(brightness)
            sliderType = SliderType.BRIGHTNESS

            setProgressChangedCustomCallback { progress, fromUser ->
                if (fromUser) {
                    setBrightness(progress)
                }
            }

            setOnStopTrackingTouchCustomCallback { progress ->
                Logs.d(TAG, "Brightness saved on slider release: $progress")
            }
        }
    }

    private fun getMinPhoneCallVolume(): Int {
        if (minPhoneCallVolume != null) {
            return minPhoneCallVolume as Int
        }
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) ?: 1

        val originalVolume = audioManager?.getStreamVolume(AudioManager.STREAM_VOICE_CALL) ?: 0

        try {
            audioManager?.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0)
            val actualMinimum = audioManager?.getStreamVolume(AudioManager.STREAM_VOICE_CALL) ?: 0

            audioManager?.setStreamVolume(AudioManager.STREAM_VOICE_CALL, originalVolume, 0)

            minPhoneCallVolume = normalizeVolume(actualMinimum, maxVolume)
            Logs.d(TAG, "detected minimum=$minPhoneCallVolume%")
            return minPhoneCallVolume as Int
        } catch (e: Exception) {
            Logs.e(TAG, "error detecting minimum, using min_ringtone_volume: ${e.message}")
            return MIN_RINGTONE_VOLUME_PERCENT
        }
    }

    private fun getPhoneCallVolume(): Int {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_VOICE_CALL) ?: 0
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) ?: 1
        val normalizedVolume = normalizeVolume(currentVolume, maxVolume)
        Logs.d(TAG, "getPhoneCallVolume: currentVolume=$currentVolume, maxVolume=$maxVolume, normalizedVolume=$normalizedVolume")

        val minVolume = getMinPhoneCallVolume()
        return if (normalizedVolume in 0 until minVolume) {
            minVolume
        } else {
            normalizedVolume
        }
    }

    private fun getNotificationVolume(): Int {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_NOTIFICATION) ?: 0
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) ?: 1
        val normalizedVolume = normalizeVolume(currentVolume, maxVolume)
        Logs.d(TAG, "getNotificationVolume: currentVolume=$currentVolume, maxVolume=$maxVolume, normalizedVolume=$normalizedVolume")

        return if (vModel.linkedVolumesFlow.value == true && normalizedVolume in 0 until MIN_RINGTONE_VOLUME_PERCENT) {
            MIN_RINGTONE_VOLUME_PERCENT
        } else {
            normalizedVolume
        }
    }

    private fun getSystemVolume(): Int {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 1
        val normalizedVolume = normalizeVolume(currentVolume, maxVolume)
        Logs.d(TAG, "getSystemVolume: currentVolume=$currentVolume, maxVolume=$maxVolume, normalizedVolume=$normalizedVolume")
        return normalizedVolume
    }

    private fun getRingtoneVolume(): Int {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_RING) ?: 0
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_RING) ?: 1
        val normalizedVolume = normalizeVolume(currentVolume, maxVolume)
        Logs.d(TAG, "getRingtoneVolume: currentVolume=$currentVolume, maxVolume=$maxVolume, normalizedVolume=$normalizedVolume")

        return if (normalizedVolume in 0 until MIN_RINGTONE_VOLUME_PERCENT) {
            MIN_RINGTONE_VOLUME_PERCENT
        } else {
            normalizedVolume
        }
    }

    private fun setPhoneCallVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) {
            Logs.d(TAG, "setPhoneCallVolume: Volume change blocked due to DND or silent mode")
            return
        }

        val minVolume = getMinPhoneCallVolume()
        val adjustedVolume = if (newVolume in 0 until minVolume) {
            minVolume
        } else {
            newVolume
        }

        if (vBinding.ccsPhoneCallVolume.getProgress() != adjustedVolume) {
            vBinding.ccsPhoneCallVolume.setProgress(adjustedVolume)
        }

        try {
            (context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).let {
                val maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
                val denormalizedVolume = denormalizeVolume(adjustedVolume, maxVolume)
                it.setStreamVolume(AudioManager.STREAM_VOICE_CALL, denormalizedVolume, AudioManager.FLAG_SHOW_UI)

                syncSliderWithActualVolume(vBinding.ccsPhoneCallVolume, AudioManager.STREAM_VOICE_CALL)
            }
        } catch (e: SecurityException) {
            Logs.e(TAG, "setPhoneCallVolume: Permission denied: ${e.message}")
            iMainActivity.showToast(R.string.error_permission_denied)
        } catch (e: Exception) {
            Logs.e(TAG, "setPhoneCallVolume: Exception: ${e.message}")
            iMainActivity.showToast(R.string.error_generic)
        }
    }

    private fun setNotificationVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) {
            Logs.d(TAG, "setNotificationVolume: Volume change blocked due to DND or silent mode")
            return
        }
        try {
            (context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).let {
                val maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
                val adjustedVolume = if (vModel.linkedVolumesFlow.value == true && (newVolume in 0 until MIN_RINGTONE_VOLUME_PERCENT)) {
                    MIN_RINGTONE_VOLUME_PERCENT
                } else {
                    newVolume
                }

                if (vBinding.ccsMessagesVolume.getProgress() != adjustedVolume) {
                    vBinding.ccsMessagesVolume.setProgress(adjustedVolume)
                }

                val denormalizedVolume = denormalizeVolume(adjustedVolume, maxVolume)
                Logs.d(TAG, "setNotificationVolume: Setting notification volume to $denormalizedVolume (normalized: $adjustedVolume, max: $maxVolume)")
                it.setStreamVolume(AudioManager.STREAM_NOTIFICATION, denormalizedVolume, AudioManager.FLAG_SHOW_UI)

                syncSliderWithActualVolume(vBinding.ccsMessagesVolume, AudioManager.STREAM_NOTIFICATION)
                if (vModel.linkedVolumesFlow.value == true) {
                    syncSliderWithActualVolume(vBinding.ccsRingtoneVolume, AudioManager.STREAM_RING)
                }
            }
        } catch (e: SecurityException) {
            Logs.e(TAG, "setNotificationVolume: Permission denied: ${e.message}")
            iMainActivity.showToast(R.string.error_permission_denied)
        } catch (e: Exception) {
            Logs.e(TAG, "setNotificationVolume: Exception: ${e.message}")
            iMainActivity.showToast(R.string.error_generic)
        }
    }

    private fun setSystemVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) {
            Logs.d(TAG, "setSystemVolume: Volume change blocked due to DND or silent mode")
            return
        }
        try {
            (context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).let { audioManager ->
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val denormalizedVolume = denormalizeVolume(newVolume, maxVolume)
                Logs.d(TAG, "setSystemVolume: Setting system volume to $denormalizedVolume (normalized: $newVolume, max: $maxVolume)")
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, denormalizedVolume, AudioManager.FLAG_SHOW_UI)
                syncSliderWithActualVolume(vBinding.ccsSystemVolume, AudioManager.STREAM_MUSIC)
            }
        } catch (e: SecurityException) {
            Logs.e(TAG, "setSystemVolume: Permission denied: ${e.message}")
            iMainActivity.showToast(R.string.error_permission_denied)
        } catch (e: Exception) {
            Logs.e(TAG, "setSystemVolume: Exception: ${e.message}")
            iMainActivity.showToast(R.string.error_generic)
        }
    }

    private fun setRingtoneVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) {
            Logs.d(TAG, "setRingtoneVolume: Volume change blocked due to DND or silent mode")
            return
        }

        val adjustedVolume = if (newVolume in 0 until MIN_RINGTONE_VOLUME_PERCENT) {
            MIN_RINGTONE_VOLUME_PERCENT
        } else {
            newVolume
        }

        if (vBinding.ccsRingtoneVolume.getProgress() != adjustedVolume) {
            vBinding.ccsRingtoneVolume.setProgress(adjustedVolume)
        }

        try {
            (context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).let { audioManager ->
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                val denormalizedVolume = denormalizeVolume(adjustedVolume, maxVolume)
                Logs.d(TAG, "setRingtoneVolume: Setting ringtone volume to $denormalizedVolume (normalized: $adjustedVolume, max: $maxVolume)")
                audioManager.setStreamVolume(AudioManager.STREAM_RING, denormalizedVolume, AudioManager.FLAG_SHOW_UI)

                syncSliderWithActualVolume(vBinding.ccsRingtoneVolume, AudioManager.STREAM_RING)
                if (vModel.linkedVolumesFlow.value == true) {
                    syncSliderWithActualVolume(vBinding.ccsMessagesVolume, AudioManager.STREAM_NOTIFICATION)
                }
            }
        } catch (e: SecurityException) {
            Logs.e(TAG, "setRingtoneVolume: Permission denied: ${e.message}")
            iMainActivity.showToast(R.string.error_permission_denied)
        } catch (e: Exception) {
            Logs.e(TAG, "setRingtoneVolume: Exception: ${e.message}")
            iMainActivity.showToast(R.string.error_generic)
        }
    }

    private fun isDoNotDisturbModeEnabled(): Boolean {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return notificationManager?.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    private fun isSilentModeEnabled(): Boolean {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        return audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT
    }

    private fun normalizeVolume(volume: Int, maxVolume: Int): Int {
        if (maxVolume <= 0) {
            Logs.e(TAG, "normalizeVolume: maxVolume is invalid ($maxVolume)")
            return 0
        }
        val normalized = ((volume.toFloat() * 100) / maxVolume).roundToInt().coerceIn(0, 100)
        Logs.d(TAG, "normalizeVolume: volume=$volume, maxVolume=$maxVolume, normalized=$normalized")
        return normalized
    }

    private fun denormalizeVolume(normalizedVolume: Int, maxVolume: Int): Int {
        if (maxVolume <= 0) {
            Logs.e(TAG, "denormalizeVolume: maxVolume is invalid ($maxVolume)")
            return 0
        }
        val denormalized = ((normalizedVolume.toFloat() * maxVolume) / 100).roundToInt().coerceIn(0, maxVolume)
        Logs.d(TAG, "denormalizeVolume: normalizedVolume=$normalizedVolume, maxVolume=$maxVolume, denormalized=$denormalized")
        return denormalized
    }

    private fun syncSliderWithActualVolume(sliderView: CustomSettingsSlider, streamType: Int) {
        try {
            (context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager).let { audioManager ->
                val actualVolume = audioManager.getStreamVolume(streamType)
                val maxVolume = audioManager.getStreamMaxVolume(streamType)
                val normalizedActual = normalizeVolume(actualVolume, maxVolume)

                if (sliderView.getProgress() != normalizedActual) {
                    sliderView.setProgress(normalizedActual)
                    Logs.d(TAG, "syncSliderWithActualVolume: Synced slider to actual volume $normalizedActual% (stream=$streamType)")
                }
            }
        } catch (e: Exception) {
            Logs.e(TAG, "syncSliderWithActualVolume: Error syncing slider: ${e.message}")
        }
    }

    private fun refreshAllSliders() {
        try {
            syncSliderWithActualVolume(vBinding.ccsPhoneCallVolume, AudioManager.STREAM_VOICE_CALL)
            syncSliderWithActualVolume(vBinding.ccsSystemVolume, AudioManager.STREAM_MUSIC)
            syncSliderWithActualVolume(vBinding.ccsMessagesVolume, AudioManager.STREAM_NOTIFICATION)
            syncSliderWithActualVolume(vBinding.ccsRingtoneVolume, AudioManager.STREAM_RING)

            Logs.d(TAG, "refreshAllSliders: All sliders refreshed with actual system values")
        } catch (e: Exception) {
            Logs.e(TAG, "refreshAllSliders: Error refreshing sliders: ${e.message}")
        }
    }

    fun isAutoBrightnessEnabled(context: Context?): Boolean {
        if (context == null) return false
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    }

    private fun getBrightness(): Int {
        return try {
            val brightness = Settings.System.getInt(
                context?.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                -1
            )

            if (brightness in 0..255) {
                Logs.d(TAG, "getBrightness: System brightness=$brightness")
                brightness
            } else {
                Logs.e(TAG, "getBrightness: Invalid brightness value ($brightness)")
                0
            }
        } catch (e: Exception) {
            Logs.e(TAG, "getBrightness: Failed to get system brightness: ${e.message}")
            0
        }
    }

    private fun setBrightness(newBrightness: Int) {
        if (isAutoBrightnessEnabled(context)) {
            // Desactivar brillo automático
            Settings.System.putInt(
                context?.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        }

        try {
            if (Settings.System.canWrite(context)) {
                Settings.System.putInt(
                    context?.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    newBrightness
                )
                Logs.d("Brightness", "System brightness set to $newBrightness")
            } else {
                Logs.e("Brightness", "No permission to write system settings")
            }
        } catch (e: Exception) {
            Logs.e("Brightness", "Error setting brightness: ${e.message}")
        }
    }

    private fun checkSystemWriteSettings() {
        if (!vModel.hasAudioPermissions()) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context?.packageName}"))
            context?.startActivity(intent)
        } else {
            Logs.d(TAG, "checkSystemWriteSettings: System write settings already granted")
        }
    }
}