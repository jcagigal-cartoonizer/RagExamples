package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.ConfigurationScreenPasswordUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.framework.sdk.model.UserInfo
import ifac.td.taxi.framework.sdk.usecase.ConfigurationUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginUserViewModel(
    private val externalBrigde: ExternalBridgeInterface,
    private val configuration: ConfigurationUseCase,
    private val sessionUseCase: SessionUseCase,
    private val configurationScreenPasswordUseCase: ConfigurationScreenPasswordUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "LoginUserViewModel"

    private val _loginDataFlow = MutableSharedFlow<Pair<String?, String?>>()
    val loginDataFlow = _loginDataFlow.asSharedFlow()

    private val _correctPasswordFlow = MutableSharedFlow<Boolean>()
    val correctPasswordFlow = _correctPasswordFlow.asSharedFlow()

    private val _configurationPasswordDialogFlow = MutableStateFlow<Boolean?>(null)
    val configurationPasswordDialogFlow = _configurationPasswordDialogFlow.asStateFlow()

    fun loginUser(user: String, password: String, fromMigration: Boolean = false) {
        viewModelScope.launch {
            if (!isInternetConnectionAvailable(context)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                }

                navigateBack()
                Logs.e(TAG, "loginUser: Network connection not available")
                return@launch
            }

            externalBrigde.setUsername(user)
            externalBrigde.setPassword(password)

            configuration.downloadConfiguration(
                userInfo = UserInfo(
                    user = user, password = password
                ),
                fromMigration = fromMigration
            )
        }
    }

    fun clickChangePassword() {
        viewModelScope.launch {
            navigateTo(R.id.action_loginUserFragment_to_changeUserPasswordFragment)
        }
    }

    fun downloadBravoConfiguration() {
        viewModelScope.launch {
            configuration.downloadBravoconfiguration()
        }
    }

    fun checkSavedData() {
        viewModelScope.launch {
            val session = sessionUseCase.getSession()
            session?.username.let { user ->
                session?.password.let { password ->
                    _loginDataFlow.emit(Pair(user, password))
                }
            }
        }
    }

    fun checkHasSettingsPassword() {
        viewModelScope.launch {
            val hasPassword = configurationScreenPasswordUseCase.hasSettingsPassword()
            _configurationPasswordDialogFlow.emit(hasPassword)
        }
    }

    fun checkSettingsPassword(pin: String) {
        viewModelScope.launch {
            val userPassword = configurationScreenPasswordUseCase.encryptSettingsPassword(pin)
            val savedPassword = configurationScreenPasswordUseCase.getSettingsPassword()
            _correctPasswordFlow.emit(userPassword == savedPassword)
        }
    }

    fun migrateConfigsUser() {
        viewModelScope.launch {
            migrationV2UseCase.migrateUserConfigPrefs()
        }
    }

    fun migrateShiftsAndTrips() {
        viewModelScope.launch {
            val allShifts = migrationV2UseCase.getAllShifts()
            Logs.d("Migration", "val allShifts: $this")

            if (allShifts.isNotEmpty()) {
                migrationV2UseCase.migrateShifts(allShifts)
            }
            migrateTrips()
        }
    }

    suspend fun migrateTrips() {
            val allTrips = migrationV2UseCase.getAllTrips()
            Logs.d(TAG, "migrateTrips: total trips to migrate: ${allTrips.size}")
            allTrips.forEachIndexed { index, pair ->
                Logs.d(TAG, "migrateTrips: trip index $index: ${pair.second?.id}")
                //only trip
                //if (pair.first == null) {
                    pair.second?.let { trip ->
                        Logs.d(TAG, "migrateTrips: Procesando trip con id = ${trip.id}")

                        val shiftId = trip.fkShiftId
                        val shiftExists = migrationV2UseCase.existsShiftId(shiftId)

                        if (shiftExists) {
                            migrationV2UseCase.migrateTrip(trip)
                            Logs.d(TAG, "migrateTrips: Trip insertado correctamente (shiftId=$shiftId)")
                        } else {
                            Logs.d(TAG, "migrateTrips: Trip omitido: shiftId=$shiftId no existe, se evita FOREIGN KEY error")
                        }
                    }
                //} else {
                    //with dispatch
                //}
            }

    }


    fun migratePartials() {
        viewModelScope.launch {
            migrationV2UseCase.migratePartials()
        }
    }

    fun migratePortugal() {
        viewModelScope.launch {
            if (licensingUseCase.getLicensingParameters()?.isFiscalService == true) {
                migrationV2UseCase.migratePortugal()
            }
        }
    }
}