package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.PartialModel.Companion.hasTrips
import ifac.td.taxi.domain.model.itop.ITopMeterStatus
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.PartialUseCase
import ifac.td.taxi.domain.usecase.PermissionsUseCase
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.ShiftUseCase
import ifac.td.taxi.domain.usecase.StartShiftUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.LoginDriverUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.screen.LoginUserFragmentDirections
import ifac.td.taxi.ui.screen.WelcomeFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeViewModel(
    private val sessionUseCase: SessionUseCase,
    private val shiftUseCase: ShiftUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bravoCentralConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val ticketUseCase: TicketUseCase,
    private val loginDriverUseCase: LoginDriverUseCase,
    private val startShiftUseCase: StartShiftUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val permissionsUseCase: PermissionsUseCase,
    private val partialUseCase: PartialUseCase,
    private val taximeterConnectUseCase: TaximeterConnectUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val migrationV2UseCase: MigrationV2UseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "WelcomeViewModel"

    private val _hasShiftsFlow = MutableStateFlow<Boolean?>(null)
    val hasShiftsFlow = _hasShiftsFlow.asStateFlow()

    private val _partialButtonFlow = MutableStateFlow<Boolean?>(null)
    val partialButtonFlow = _partialButtonFlow.asStateFlow()

    private val _batteryOptimizationFlow = MutableSharedFlow<Boolean>()
    val batteryOptimizationFlow = _batteryOptimizationFlow.asSharedFlow()

    private val _loginWithoutCentralWelcomeFlow = MutableSharedFlow<Boolean>()
    val loginWithoutCentralWelcomeFlow = _loginWithoutCentralWelcomeFlow.asSharedFlow()

    private val _userLogInFlow = MutableSharedFlow<Boolean>()
    val userLogInFlow = _userLogInFlow.asSharedFlow()

    private val _hasAllPermissionsFlow = MutableSharedFlow<Boolean>()
    val hasAllPermissionsFlow = _hasAllPermissionsFlow.asSharedFlow()

    private val _neddMigrationFlow = MutableSharedFlow<Boolean>()
    val neddMigrationFlow = _neddMigrationFlow.asSharedFlow()

    // Pantallas protegidas por el secure pin
    enum class SecurePinDestination {
        SHIFTS,
        STATISTICS,
    }

    // Se emite cuando ya existe un pin y hay que pedirlo antes de entrar a la pantalla indicada
    private val _requestSecurePinFlow = MutableSharedFlow<SecurePinDestination>()
    val requestSecurePinFlow = _requestSecurePinFlow.asSharedFlow()

    // Se emite cuando el pin introducido no coincide con el guardado
    private val _wrongSecurePinFlow = MutableSharedFlow<Boolean>()
    val wrongSecurePinFlow = _wrongSecurePinFlow.asSharedFlow()


    init {
        viewModelScope.launch {
            if (sessionUseCase.isUserLoggedIn()) {
                //creamos aquío las conexiones?
            }
        }
    }

    //val showLegalTextLiveData: LiveData<Boolean> get() = _showLegalTextLiveData
    //private val _showLegalTextLiveData = MutableLiveData<Boolean>()


    fun clickMenuLogin() {
        viewModelScope.launch {
            if (!sessionUseCase.isUserLoggedIn()) {
                navigateTo(R.id.action_welcomeFragment_to_loginUserFragment)
            } else {
                val conectionMode = bravoCentralConfigurationUseCase.getConnectionMode()
                //Check if one one method
                var cntMethods = 0
                //conCentral
                if ((conectionMode and 0x01) != 0) {
                    cntMethods++
                }
                //sinCentral
                if ((conectionMode and 0x02) != 0) {
                    cntMethods++
                }
                //refuerzo
                if ((conectionMode and 0x04) != 0) {
                    cntMethods++
                }

                val startTurnMode = licensingUseCase.getStartTurnMode() ?: 1

                if (startTurnMode == 0 && cntMethods < 2) {
                    //con central
                    if ((conectionMode and 0x01) != 0) {
                        loginDriverUseCase.loginDriver("0000", "", false)
                    }
                    //sinCentral
                    if ((conectionMode and 0x02) != 0) {
                        Logs.d(TAG, "loginWithoutCentral() ")
                        W2CLocation.setLocationAllowedByCentral(false)
                        Logs.d(TAG, "setLocationAllowedByCentral: false")
                        sessionUseCase.updateWithCentralAllowed(false)

                        val user = sessionUseCase.getSession()
                        withContext(Dispatchers.IO) {
                            ticketUseCase.loadTickets()
                        }

                        startShiftUseCase.startShift()
                        shiftStatusUseCase.setStatus(ifConstants.STATE_FOR_HIRE_NO_CENTRAL, false)
                        _loginWithoutCentralWelcomeFlow.emit(true)
                    }
                    //refuerzo
                    if ((conectionMode and 0x04) != 0) {
                        loginDriverUseCase.loginDriver("0000", "", true)
                    }
                } else {
                    val action =
                        WelcomeFragmentDirections.actionWelcomeFragmentToLoginDriverFragment()
                            .setConnectionMode(conectionMode).setStartTurnMode(startTurnMode)

                    navigateTo(action)
                }
            }
        }
    }

    fun clickShifts() {
        requestSecurePinIfNeeded(SecurePinDestination.SHIFTS)
    }

    fun clickStatistics() {
        requestSecurePinIfNeeded(SecurePinDestination.STATISTICS)
    }

    private fun requestSecurePinIfNeeded(destination: SecurePinDestination) {
        viewModelScope.launch {
            val currentSecurePin = userPreferencesUseCase.getSecurePin()
            if (!currentSecurePin.isNullOrEmpty()) {
                Logs.d(TAG, "checkSecure: ${encodeSecurePin(currentSecurePin)}")
                _requestSecurePinFlow.emit(destination)
            } else {
                navigateToDestination(destination)
            }
        }
    }

    fun verifyCurrentSecurePin(currentPin: String, destination: SecurePinDestination) {
        viewModelScope.launch {
            val actualPin: String? = userPreferencesUseCase.getSecurePin()
            if (currentPin == actualPin) {
                navigateToDestination(destination)
            } else {
                _wrongSecurePinFlow.emit(true)
            }
        }
    }


    private suspend fun navigateToDestination(destination: SecurePinDestination) {
        when (destination) {
            SecurePinDestination.SHIFTS ->
                navigateTo(R.id.action_welcomeFragment_to_shiftsFragment)

            SecurePinDestination.STATISTICS ->
                navigateTo(R.id.action_welcomeFragment_to_statisticsFragment)
        }
    }

    fun clickSettings() {
        viewModelScope.launch {
            navigateTo(R.id.action_welcomeFragment_to_settingsFragment)
        }
    }

    fun haveShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getLastShift()
            _hasShiftsFlow.emit(shifts != null)
        }
    }

    fun clickConfiguration() {
        viewModelScope.launch {
            navigateTo(R.id.action_welcomeFragment_to_loginUserFragment)
        }
    }

    fun checkUserLogIn() {
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = sessionUseCase.isUserLoggedIn()
            _userLogInFlow.emit(loggedIn)
        }
    }

    fun clickParciales() {
        viewModelScope.launch {
            val activePartial = partialUseCase.getActivePartial()
            if (activePartial != null && activePartial.hasTrips()) {
                Logs.d(TAG, "Navigating to active partials")
                navigateTo(R.id.action_welcomeFragment_to_partials)
            } else {
                Logs.d(TAG, "No active partials, navigating to closed partials")
                navigateTo(R.id.action_welcomeFragment_to_closed_partial)
            }
        }
    }

    fun clickPermissions() {
        viewModelScope.launch {
            navigateTo(R.id.action_welcomeFragment_to_permissionsFragment)
        }
    }

    fun checkClosuresPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            Logs.d("WelcomeViewModel", "isClosuresButton: ${parameters?.isClosuresButton}")
            _partialButtonFlow.emit(parameters?.isClosuresButton)
        }
    }

    fun checkAllPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasAllPermissions = permissionsUseCase.hasAllPermissions()
            _hasAllPermissionsFlow.emit(hasAllPermissions)
        }
    }

    fun hasITopMeterPaired(): Boolean {
        return bluetoothLocalUseCase.isCurrentBluetoothItop()
    }

    fun tryLoginDriver(lastITopCom: ITopMeterStatus?) {
        viewModelScope.launch {
            lastITopCom?.driverCardData?.let {
                if (it.length > 18) {
                    val cardData = it.substring(12, 17)
                    loginDriverUseCase.loginDriver(cardData, cardData, false)
                }
            }
        }
    }

    fun makeMigrationV2() {
        viewModelScope.launch {
            val hasOldTrips = migrationV2UseCase.checkHasOldTrip()
            val hasOldShift = migrationV2UseCase.checkHasOldShift()

                /**showDialog(
                    Pair(
                        CustomDialog.CustomDialogModel(
                            title = "No cierre la aplicación",
                            description = "Estamos migrando los datos hacia el nuevo sistema de base de datos.",
                            isCancellable = false,
                            icon = R.drawable.round_warning_24,
                        ),
                        {}
                    )
                )

                if (hasOldShift) {
                    val allShifts = migrationV2UseCase.getAllShifts()

                    if (allShifts.isNotEmpty()) {
                        migrationV2UseCase.migrateShifts(allShifts)
                    }
                }*/

            if (hasOldShift || hasOldTrips) {
                Logs.d(TAG, "Si hay datos de V2")
                val checkExistSession = sessionUseCase.getSession()
                if (checkExistSession == null) {
                    Logs.d(TAG, "No hay sessión v3")
                    if (migrationV2UseCase.migrateSession()) {
                        Toast.makeText(context, "Start migration", Toast.LENGTH_LONG).show()
                        navigateTo(WelcomeFragmentDirections.actionWelcomeFragmentToLoginUserFragment().setAutoDownloadConfigurationFromMigration(true))
                    }
                }
            } else {
                Logs.d(TAG, "No hay datos de V2")
            }
        }
    }
}