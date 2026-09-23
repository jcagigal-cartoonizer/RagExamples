package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.DeviceSettingsUiEvent
import ifac.td.taxi.ui.screen.components.DeviceSettingsButtonsState
import ifac.td.taxi.ui.screen.components.DeviceSettingsUiEffect
import ifac.td.taxi.ui.screen.components.DeviceSettingsUiState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 5-1: import android.app.Application
class DeviceSettingsComposeViewModel(
    application: Application,
    private val soundManagerUseCase: SoundManagerUseCase,
) : AndroidViewModel(application) {
    companion object {
        const val MIN_RINGTONE_VOLUME_PERCENT = 20
    }
    private val appContext: Context get() = getApplication<Application>()
    private val _uiState = MutableStateFlow(DeviceSettingsUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<DeviceSettingsUiEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffect: SharedFlow<DeviceSettingsUiEffect> = _uiEffect
    fun onEvent(event: DeviceSettingsUiEvent) {
        when (event) {
            DeviceSettingsUiEvent.OnScreenResumed -> refreshState()
            DeviceSettingsUiEvent.OnRequestWriteSettingsPermission -> emitEffect(DeviceSettingsUiEffect.OpenWriteSettings)
            DeviceSettingsUiEvent.OnDismissDialog -> _uiState.update { it.copy(dialog = null) }
            is DeviceSettingsUiEvent.OnPhoneCallVolumeChanged -> setPhoneCallVolume(event.volume)
            is DeviceSettingsUiEvent.OnSystemVolumeChanged -> setSystemVolume(event.volume)
            is DeviceSettingsUiEvent.OnNotificationVolumeChanged -> setNotificationVolume(event.volume)
            is DeviceSettingsUiEvent.OnRingtoneVolumeChanged -> setRingtoneVolume(event.volume)
            is DeviceSettingsUiEvent.OnBrightnessChanged -> setBrightness(event.brightness)
            DeviceSettingsUiEvent.OnPhoneCallStopTracking -> handleStopTracking(stream = AudioManager.STREAM_VOICE_CALL)
            DeviceSettingsUiEvent.OnSystemStopTracking -> handleStopTracking(stream = AudioManager.STREAM_MUSIC)
            DeviceSettingsUiEvent.OnNotificationStopTracking -> handleStopTracking(stream = AudioManager.STREAM_NOTIFICATION)
            DeviceSettingsUiEvent.OnRingtoneStopTracking -> {
                testRingTone()
                handleStopTracking(stream = AudioManager.STREAM_RING)
            }
            DeviceSettingsUiEvent.OnBrightnessStopTracking -> Unit
            DeviceSettingsUiEvent.OnBackPressed -> emitEffect(DeviceSettingsUiEffect.NavigateBack)
            DeviceSettingsUiEvent.OnOpenSystemWriteSettings -> emitEffect(DeviceSettingsUiEffect.OpenWriteSettings)
        }
    }
    fun refreshState() {
        val canWrite = hasAudioPermissions()
        val dnd = isDoNotDisturbModeEnabled()
        val silent = isSilentModeEnabled()
        val linked = areNotificationAndRingVolumesLinked()
        val phone = getPhoneCallVolume()
        val system = getSystemVolume()
        val notification = getNotificationVolume(linked)
        val ringtone = getRingtoneVolume()
        val brightness = getBrightness()
        _uiState.update {
            it.copy(
                canWriteSettings = canWrite,
                isDndEnabled = dnd,
                isSilentModeEnabled = silent,
                linkedVolumes = linked,
                phoneCallVolume = phone,
                systemVolume = system,
                notificationVolume = notification,
                ringtoneVolume = ringtone,
                brightness = brightness,
                buttonsState = DeviceSettingsButtonsState.from(
                    canWriteSettings = canWrite,
                    isDndEnabled = dnd,
                    isSilentModeEnabled = silent,
                    linkedVolumes = linked
                )
            )
        }
    }
    fun hasAudioPermissions(): Boolean = Settings.System.canWrite(appContext)
    fun emitEffect(effect: DeviceSettingsUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
    fun handleStopTracking(stream: Int) {
        if (!hasAudioPermissions()) {
            emitEffect(DeviceSettingsUiEffect.ShowToast(DeviceSettingsToast.ErrorPermissionDenied))
            emitEffect(DeviceSettingsUiEffect.OpenWriteSettings)
        }
    }
    fun testRingTone() {
        viewModelScope.launch {
            try {
                soundManagerUseCase.playTestSound()
            } catch (_: Exception) {
            }
        }
    }
    fun areNotificationAndRingVolumesLinked(): Boolean {
        return try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            val testVolume = if (notificationVolume > 0) notificationVolume - 1 else notificationVolume + 1
            val maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            val safeTestVolume = testVolume.coerceIn(0, maxNotificationVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, safeTestVolume, 0)
            val newRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            val areLinked = newRingVolume != ringVolume
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notificationVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, ringVolume, 0)
            areLinked
        } catch (_: Exception) {
            false
        }
    }
    fun getPhoneCallVolume(): Int = normalizeStream(AudioManager.STREAM_VOICE_CALL, min = true)
    fun getSystemVolume(): Int = normalizeStream(AudioManager.STREAM_MUSIC)
    fun getNotificationVolume(linked: Boolean): Int {
        val value = normalizeStream(AudioManager.STREAM_NOTIFICATION)
        return if (linked && value in 0 until MIN_RINGTONE_VOLUME_PERCENT) MIN_RINGTONE_VOLUME_PERCENT else value
    }
    fun getRingtoneVolume(): Int {
        val value = normalizeStream(AudioManager.STREAM_RING)
        return if (value in 0 until MIN_RINGTONE_VOLUME_PERCENT) MIN_RINGTONE_VOLUME_PERCENT else value
    }
    fun normalizeStream(stream: Int, min: Boolean = false): Int {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(stream)
        val maxVolume = audioManager.getStreamMaxVolume(stream)
        val normalized = if (maxVolume <= 0) 0 else ((currentVolume.toFloat() * 100) / maxVolume).toInt().coerceIn(0, 100)
        return normalized
    }
    fun denormalizeVolume(normalizedVolume: Int, maxVolume: Int): Int {
        if (maxVolume <= 0) return 0
        return ((normalizedVolume.toFloat() * maxVolume) / 100).toInt().coerceIn(0, maxVolume)
    }
    fun setPhoneCallVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) return
        try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            audioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                denormalizeVolume(newVolume.coerceAtLeast(MIN_RINGTONE_VOLUME_PERCENT), maxVolume),
                AudioManager.FLAG_SHOW_UI
            )
            refreshState()
        } catch (_: Exception) {}
    }
    fun setSystemVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) return
        try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                denormalizeVolume(newVolume, maxVolume),
                AudioManager.FLAG_SHOW_UI
            )
            refreshState()
        } catch (_: Exception) {}
    }
    fun setNotificationVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) return
        try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            val adjusted = if (uiState.value.linkedVolumes && newVolume in 0 until MIN_RINGTONE_VOLUME_PERCENT) {
                MIN_RINGTONE_VOLUME_PERCENT
            } else newVolume
            audioManager.setStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                denormalizeVolume(adjusted, maxVolume),
                AudioManager.FLAG_SHOW_UI
            )
            refreshState()
        } catch (_: Exception) {}
    }
    fun setRingtoneVolume(newVolume: Int) {
        if (isDoNotDisturbModeEnabled() || isSilentModeEnabled()) return
        try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            val adjusted = newVolume.coerceAtLeast(MIN_RINGTONE_VOLUME_PERCENT)
            audioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                denormalizeVolume(adjusted, maxVolume),
                AudioManager.FLAG_SHOW_UI
            )
            refreshState()
        } catch (_: Exception) {}
    }
    fun setBrightness(newBrightness: Int) {
        try {
            if (Settings.System.canWrite(appContext)) {
                Settings.System.putInt(appContext.contentResolver, Settings.System.SCREEN_BRIGHTNESS, newBrightness.coerceIn(0, 255))
                refreshState()
            }
        } catch (_: Exception) {}
    }
    fun isDoNotDisturbModeEnabled(): Boolean {
        val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        return nm.currentInterruptionFilter != android.app.NotificationManager.INTERRUPTION_FILTER_ALL
    }
    fun isSilentModeEnabled(): Boolean {
        val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.ringerMode == AudioManager.RINGER_MODE_SILENT
    }
    fun getBrightness(): Int = try {
        Settings.System.getInt(appContext.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0).coerceIn(0, 255)
    } catch (_: Exception) {
        0
    }
}
