package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AboutViewModel(
    context: Application,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
) : BaseViewModel(context) {

    private val TAG = "AboutViewModel"

    private val _deviceFlow = MutableSharedFlow<Pair<Boolean, BluetoothInfo?>>()
    val deviceFlow = _deviceFlow.asSharedFlow()

    private val _warningFlow = MutableSharedFlow<Boolean>()
    val warningFlow = _warningFlow.asSharedFlow()

    fun clickPrivacy() {
        val privacyPolicyUrl = "https://www.taxitronic.com/en/privacy-policy-smart-td/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            viewModelScope.launch {
                _warningFlow.emit(true)
            }
            Logs.d(TAG, "clickPrivacy: No hay navegador disponible para abrir este enlace")
        }
    }

    fun checkSavedDevice() {
        viewModelScope.launch {
            val bluetooth = bluetoothLocalUseCase.getLocalBluetooth()
            val isDeviceAvailable = bluetooth != null
            _deviceFlow.emit(Pair(isDeviceAvailable, bluetooth))
        }
    }
}
