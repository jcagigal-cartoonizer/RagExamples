package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.sdk.usecase.SoundManagerUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceSettingsViewModel(
    private val soundManagerUseCase: SoundManagerUseCase,
    context: Application,
): BaseViewModel(context) {

    private val TAG = "DeviceSettingsViewModel"

    private val _linkedVolumesFlow = MutableStateFlow<Boolean?>(null)
    val linkedVolumesFlow = _linkedVolumesFlow.asStateFlow()

    fun hasAudioPermissions(): Boolean {
        return context != null && Settings.System.canWrite(context)
    }

    fun checkLinkedVolumes() {
        viewModelScope.launch {
            val linkedVolumes = areNotificationAndRingVolumesLinked()
            _linkedVolumesFlow.emit(linkedVolumes)
        }
    }

    fun testRingTone() {
        viewModelScope.launch {
            try {
                soundManagerUseCase.playTestSound()
            } catch (e: Exception) {
                Logs.e("TAG", "Error playing ringtone: ${e.message}")
            }
        }
    }

    private fun areNotificationAndRingVolumesLinked(): Boolean {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Current volumes
            val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)

            // Change notification volume
            val testVolume =
                if (notificationVolume > 0) notificationVolume - 1 else notificationVolume + 1
            val maxNotificationVolume =
                audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            val safeTestVolume = testVolume.coerceIn(0, maxNotificationVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, safeTestVolume, 0)

            // Check if ring volume changed
            val newRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            val areLinked = newRingVolume != ringVolume

            // Restore original volumes
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notificationVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, ringVolume, 0)

            Logs.d("DeviceSettingsViewModel", "Volume streams linked: $areLinked")
            return areLinked

        } catch (e: Exception) {
            Logs.e("DeviceSettingsViewModel", "Error checking linked volumes: ${e.message}")
            return false
        }
    }
}