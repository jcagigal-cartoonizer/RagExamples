package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.viewModelScope
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.domain.model.RedSysLoginResponse
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.PermissionRequest
import ifac.td.taxi.framework.PermissionRequest.PermissionTypeList.PERMISSION_IGNORE_BATTERY_OPTIMIZATIONS
import ifac.td.taxi.framework.TemporalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PermissionsViewModel(
    context: Application,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val redSysUseCase: RedSysUseCase,
) : BaseViewModel(context) {

    private val TAG = "PermissionsViewModel"

    private val _permissionGrantedFlow = MutableSharedFlow<Pair<Boolean?, String?>>()
    val permissionGrantedFlow = _permissionGrantedFlow.asSharedFlow()

    private val _redSysConfigurationFlow = MutableStateFlow<Boolean>(false)
    val redSysConfigurationFlow = _redSysConfigurationFlow.asStateFlow()

    private val _phoneNumberConfigurationFlow = MutableSharedFlow<Boolean>()
    val phoneNumberConfigurationFlow = _phoneNumberConfigurationFlow.asSharedFlow()

    private val _deviceSettingsPermissionFlow = MutableSharedFlow<Boolean>()
    val deviceSettingsPermissionFlow = _deviceSettingsPermissionFlow.asSharedFlow()

    private val _overloadPermissionFlow = MutableSharedFlow<Boolean>()
    val overloadPermissionFlow = _overloadPermissionFlow.asSharedFlow()

    private val _redSysUsernameFlow = MutableStateFlow<String?>(null)
    val redSysUsernameFlow = _redSysUsernameFlow.asStateFlow()

    fun checkPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            val permissionList = PermissionRequest.returnPermissionTypeList()
            Logs.d("PermissionsViewModel", "Permission List: $permissionList")
            permissionList.forEach { permission ->
                val permissionStatus = when (permission) {
                    PERMISSION_IGNORE_BATTERY_OPTIMIZATIONS -> {
                        val isOptimizationDisabled = isBatteryOptimizationDisabled()
                        Logs.d(
                            "PermissionsViewModel",
                            "Permission PERMISSION_IGNORE_BATTERY_OPTIMIZATIONS: ($isOptimizationDisabled, ${permission.stringPermission})"
                        )
                        isOptimizationDisabled
                    }
                    else -> PermissionRequest.checkHavePermission(permission, context)
                }

                _permissionGrantedFlow.emit(permissionStatus to permission.stringPermission)
                Logs.d("PermissionsViewModel", "Emitted $permissionStatus for ${permission.stringPermission}")
            }
        }
    }


    private fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    fun checkRedSysInstalled() {
        viewModelScope.launch(Dispatchers.IO) {
            val isConfigured = haveRedSysConfigured()
            _redSysConfigurationFlow.emit(isConfigured)
        }
    }


    private suspend fun haveRedSysConfigured(): Boolean {
        val userPreferences = userPreferencesUseCase.getUserPreferences()
        return userPreferences?.let {
            it.pinPadSerialNumber.isNotBlank() || it.pinPadSerialNumber.isNotEmpty()
        } ?: false
    }

    fun checkPhoneNumberConfigured() {
        viewModelScope.launch(Dispatchers.IO) {
            val isConfigured = havePhoneNumberConfigured()
            _phoneNumberConfigurationFlow.emit(isConfigured)
        }
    }


    private suspend fun havePhoneNumberConfigured(): Boolean {
        val userPreferences = userPreferencesUseCase.getUserPreferences()
        return userPreferences?.let {
            it.phoneCall.isNotBlank() || it.phoneCall.isNotEmpty()
        } ?: false
    }

    fun checkOverloadPermission() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                _overloadPermissionFlow.emit(Settings.canDrawOverlays(context))
            } else {
                _overloadPermissionFlow.emit(true)
            }
        }
    }

    fun checkSettingsDevicePermission() {
        viewModelScope.launch {
            _deviceSettingsPermissionFlow.emit(Settings.System.canWrite(context))
        }
    }

    fun saveUsernameRedSys(user: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "saveUsernameRedSys: user = $user")
            redSysUseCase.saveUsernameRedSys(user)
        }
    }

    fun checkLoginRedsys(username: String, callbackExpiredPassword: (RedSysLoginResponse)->Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            redSysUseCase.configureRedSys()
            redSysUseCase.getUsernameRedSys()?.let {
                Logs.d(TAG, "checkLoginRedsys: username = $it")
                Logs.d(TAG, "checkLoginRedsys: password = ${TemporalData.passwordRedSys}")
                val loginRedsysRespond = redSysUseCase.loginRedSys(username, TemporalData.passwordRedSys)
                when (loginRedsysRespond) {
                    is RedSysLoginResponse.Error -> {
                        callbackExpiredPassword.invoke(loginRedsysRespond)
                    }
                    is RedSysLoginResponse.Success -> {
                        TemporalData.redsysLogin = loginRedsysRespond
                        Logs.d(TAG, "checkLoginRedsys: Success")
                    }
                }
            }
        }
    }

    fun getUsernameRedSys() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "getUsernameRedSys: user = ${redSysUseCase.getUsernameRedSys()}")
            _redSysUsernameFlow.emit(redSysUseCase.getUsernameRedSys())
        }
    }
}