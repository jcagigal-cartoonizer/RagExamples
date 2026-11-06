package ifac.td.taxi.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import es.redsys.paysys.Operative.Managers.RedCLSDccSelectionData
import es.redsys.paysys.Operative.Managers.RedCLSDeferPaymentData
import es.redsys.paysys.Operative.RedCLSPinPadInterface
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.PaymentDirections
import ifac.td.taxi.R
import ifac.td.taxi.domain.mapper.DataToViewModel.Companion.toModel
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.PaymentModifiers
import ifac.td.taxi.domain.model.PrimeType
import ifac.td.taxi.domain.model.RedSysDCC
import ifac.td.taxi.domain.model.RedSysLoginResponse
import ifac.td.taxi.domain.model.RedSysSessionState
import ifac.td.taxi.domain.model.Subscriber
import ifac.td.taxi.domain.model.TTSType.Amount
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.domain.usecase.BackToDispatchedUseCase
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.CountdownManagerUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.IngenicoUseCase
import ifac.td.taxi.domain.usecase.LightSkyGlassUseCase
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.SubscriberUseCase
import ifac.td.taxi.domain.usecase.SumUpUseCase
import ifac.td.taxi.domain.usecase.TTSUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.model.BluetoothInfo
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.SoundManagerUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterManagerUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.receivers.utils.ToolUtils
import ifac.td.taxi.repository.room.dao.DispatchExtraDataDao
import ifac.td.taxi.repository.room.entities.BravoConfigurationEntity
import ifac.td.taxi.repository.room.entities.DispatchExtraDataEntity
import ifac.td.taxi.repository.room.entities.countdown.CountdownId
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.dialog.CustomDialog.CustomDialogTAG.SUBSCRIBER_RESPONSE_SUCCESS_DIALOG
import ifac.td.taxi.ui.screen.PaymentFragmentDirections
import ifac.td.taxi.viewmodel.model.CountdownModel
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.viewmodel.model.RedSysPaymentState
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toPaymentPrimeMethod
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.trimUserFromString
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentViewModel(
    private val tripUseCase: TripUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val countdownManagerUseCase: CountdownManagerUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val lightSkyGlassUseCase: LightSkyGlassUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val subscriberUseCase: SubscriberUseCase,
    private val ticketUseCase: TicketUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val sumUpUseCase: SumUpUseCase,
    private val redSysUseCase: RedSysUseCase,
    private val dispatchExtraDataDao: DispatchExtraDataDao,
    private val ttsUseCase: TTSUseCase,
    private val bravoConfigurationUseCase: BravoCentralConfigurationUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val backToDispatchedUseCase: BackToDispatchedUseCase,
    private val ingenicoUseCase: IngenicoUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val taximeterManagerUseCase: TaximeterManagerUseCase,
    private val soundManagerUseCase: SoundManagerUseCase,
    private val redSysSessionState: RedSysSessionState,
    context: Application,
) : BaseViewModel(context), RedCLSPinPadInterface {

    private val TAG = "PaymentViewModel"

    private val _conditionsLocalFlow = MutableStateFlow<Pair<BluetoothInfo?, Int>?>(null)
    val conditionsLocalFlow = _conditionsLocalFlow.asStateFlow()

    private val _uvLightFlow = MutableSharedFlow<Boolean>()
    val uvLightFlow = _uvLightFlow.asSharedFlow()

    private val _timerFlow = MutableSharedFlow<Int?>()
    val timerFlow = _timerFlow.asSharedFlow()

    private val _noCardConfiguredFlow = MutableSharedFlow<Boolean>()
    val noCardConfiguredFlow = _noCardConfiguredFlow.asSharedFlow()

    private val _countdownCallbackFlow = MutableSharedFlow<Boolean>()
    val countdownCallbackFlow = _countdownCallbackFlow.asSharedFlow()

    private var countdownModel: CountdownModel? = null

    private val _paymentModifiersFlow = MutableStateFlow<PaymentModifiers?>(null)
    val paymentModifiersFlow = _paymentModifiersFlow.asStateFlow()

    private val _checkSubscriberCreditLimitFlow = MutableSharedFlow<String>()
    val checkSubscriberCreditLimitFlow = _checkSubscriberCreditLimitFlow.asSharedFlow()

    private val _redSysStartFlow = MutableSharedFlow<Pair<Int, String?>>()
    val redSysStartFlow = _redSysStartFlow.asSharedFlow()

    private val _sumUpTicketFlow = MutableSharedFlow<String>()
    val sumUpTicketFlow = _sumUpTicketFlow.asSharedFlow()

    private val _hasMoneiAccount = MutableStateFlow(false)
    val hasMoneiAccount = _hasMoneiAccount.asStateFlow()

    private val _dispatchExtraDataFlow = MutableSharedFlow<DispatchExtraDataEntity?>()
    val dispatchExtraDataFlow = _dispatchExtraDataFlow.asSharedFlow()

    private val _showTTSFlow = MutableSharedFlow<Int>()
    val showTTSFlow = _showTTSFlow.asSharedFlow()

    private val _backToDispatchedFlow = MutableSharedFlow<Pair<Int?, Boolean>>()
    val backToDispatchedFlow = _backToDispatchedFlow.asSharedFlow()

    private val _ingenicoSubscriberFlow = MutableSharedFlow<Intent>()
    val ingenicoSubscriberFlow = _ingenicoSubscriberFlow.asSharedFlow()

    private val _ingenicoCardFlow = MutableSharedFlow<Intent>()
    val ingenicoCardFlow = _ingenicoCardFlow.asSharedFlow()

    private val _cardButtonStatusFlow = MutableSharedFlow<CustomButton.StyleButton>()
    val cardButtonStatusFlow = _cardButtonStatusFlow.asSharedFlow()

    private val _multiDispatchNavigate = MutableSharedFlow<Boolean?>()
    val multiDispatchNavigate = _multiDispatchNavigate.asSharedFlow()

    private val _subscriberAttemptsFlow =  MutableSharedFlow<Int>()
    val subscriberAttemptsFlow = _subscriberAttemptsFlow.asSharedFlow()

    private val _isIngenicoInstalledFlow = MutableStateFlow<Boolean>(false)
    val isIngenicoInstalledFlow = _isIngenicoInstalledFlow.asStateFlow()

    private val _isWaitingAppPaymentResponseFlow = MutableStateFlow(true)
    val isWaitingAppPaymentResponseFlow = _isWaitingAppPaymentResponseFlow.asStateFlow()

    private val _redSysPaymentStateFlow = MutableStateFlow(RedSysPaymentState.IDLE)
    val redSysPaymentStateFlow = _redSysPaymentStateFlow.asStateFlow()

    private val _userPreferencesFlow = MutableStateFlow<UserPreferences?>(null)
    val userPreferencesFlow = _userPreferencesFlow.asStateFlow()

    private val _bravoConfigurationFlow = MutableStateFlow<BravoConfigurationEntity?>(null)
    val bravoConfigurationFlow = _bravoConfigurationFlow.asStateFlow()

    fun setRedSysPaymentState(state: RedSysPaymentState) {
        _redSysPaymentStateFlow.value = state
    }

    fun resetRedSysPaymentState() {
        _redSysPaymentStateFlow.value = RedSysPaymentState.IDLE
    }

    fun setIsWaitingAppPaymentResponse(isWaiting: Boolean) {
        _isWaitingAppPaymentResponseFlow.value = isWaiting
    }

    private var invoiceRedSys: String? = null
    private var amountRedSys: Int? = null
    private var primeTripTypeSelected: PrimeType? = null
    var isManualService: Boolean = false

    val minimumImportAirport: Int? by lazy {
        licensingUseCase.getLicensingParameters()?.minAirportAmount
    }

    val isMinAirportSupplAmount: Boolean by lazy {
        licensingUseCase.getLicensingParameters()?.isMinAirportSupplAmount ?: true
    }

    fun initPaymentView() {
        viewModelScope.launch {
            _paymentModifiersFlow.emit(licensingUseCase.getPaymentModifiers())
            bravoConfigurationUseCase.getBravoConfigurationVariable()?.moneiAccountId?.length?.let {
                _hasMoneiAccount.emit(it > 1)
            }

            _bravoConfigurationFlow.emit(bravoCentralUseCase.getBravoConfiguration()) //?.allowsSubscriber

            _isIngenicoInstalledFlow.emit(ingenicoUseCase.hasIngenicoInstalled())

            userPreferencesUseCase.getUserPreferences()?.let {
                _userPreferencesFlow.emit(it)
            }
        }
    }

    fun checkBackToDispatched(idDispatch: Long) {
        viewModelScope.launch {
            val backPendings = backToDispatchedUseCase.getPendingBackToDispatched(idDispatch = idDispatch)
            Logs.d(TAG, "returnTodispatch checkBackToDispatched: $backPendings")
            _backToDispatchedFlow.emit(Pair(backPendings, false))
        }

    }

    fun stopCountdown() {
        viewModelScope.launch {
            Logs.e("CountdownManager", "stopCountdown: from paymentViewModel")
            _timerFlow.emit(null)
            countdownManagerUseCase.stopCountdownById(CountdownId.PAYMENT)
            countdownManagerUseCase.deleteCountdownById(CountdownId.PAYMENT)
            countdownManagerUseCase.stopCountdownById(CountdownId.WARNING_PAYMENT_SUBSCRIBER)
            countdownManagerUseCase.deleteCountdownById(CountdownId.WARNING_PAYMENT_SUBSCRIBER)
        }
    }

    fun finishPayment(
        trip: Trip?,
        paymentMethod: PaymentMethod,
        navigate: Boolean = true,
        dispatch: InfoDispatchModel?,
        isIngenico: Boolean = false,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "finishPayment: Starting with PaymentMethod=${paymentMethod.name}, navigate=$navigate, isIngenico=$isIngenico, tripId=${trip?.id}, dispatchId=${dispatch?.id}")

            trip?.let { trip ->
                Logs.d(TAG, "finishPayment: Processing tripId=${trip.id}")

                withContext(Dispatchers.IO) {
                    Logs.d(TAG, "finishPayment: Ending trip")
                    tripUseCase.endTrip(trip, paymentMethod)
                }

                Logs.d(TAG, "finishPayment: Not Ingenico. Preparing ticket...")
                withContext(Dispatchers.IO) {
                    Logs.d(TAG, "finishPayment: Preparing ticket")
                    val status = shiftStatusUseCase.getStatus()
                    status?.let {
                        Logs.d(TAG, "finishPayment: Status obtained. Calling prepareTicket.")
                        ticketUseCase.prepareTicket(
                            trip = trip,
                            dispatch = dispatch,
                            shiftStatus = status
                        )
                    } ?: run {
                        Logs.d(TAG, "finishPayment: shiftStatusUseCase.getStatus() returned null. Skipping prepareTicket.")
                    }
                }

                val countdown =
                    countdownManagerUseCase.getCountdownById(CountdownId.PAYMENT)?.toModel()

                countdown?.let {
                    Logs.d(TAG, "finishPayment: Stopping and deleting countdown")
                    countdownManagerUseCase.stopCountdown(countdown)
                    countdownManagerUseCase.deleteCountdownById(CountdownId.PAYMENT)
                    _timerFlow.emit(null)
                } ?: run {
                    Logs.d(TAG, "finishPayment: Payment countdown not found (CountdownId.PAYMENT).")
                }

                if (paymentMethod == PaymentMethod.CARD) {
                    _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                }

                if (navigate) {
                    //Navigation
                    Logs.d(TAG, "finishPayment: Navigating post payment")
                    navigatePostPayment(dispatch)
                } else {
                    Logs.d(TAG, "finishPayment: 'navigate' is false. Skipping navigation.")
                }

                Logs.d(TAG, "finishPayment: Processing completed for tripId=${trip.id}")

            } ?: run {
                Logs.d(TAG, "finishPayment: 'trip' was null. Aborting finishPayment.")
                if (paymentMethod == PaymentMethod.CARD) {
                    _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                }
            }
        }
    }

    fun subscriberPayment(trip: Trip?) {
        viewModelScope.launch(Dispatchers.IO) {
            trip?.let {
                tripUseCase.updateTotalAmount(trip.totalAmount, trip.id)
                Logs.d(TAG, "subscriberPayment: Trip is not null, stopping countdown timer")
                stopCountdownTimer()
                Logs.d(TAG, "subscriberPayment: Saving end address for the trip")
                tripUseCase.saveEndAddress(it)

                val intent = ingenicoUseCase.getIngenicoIntentSubscriber(PaymentMethod.SUBSCRIBER)
                Logs.d(TAG, "subscriberPayment: $intent")
                if (intent != null) {
                    Logs.d(TAG, "subscriberPayment: Emitting intent")
                    _ingenicoSubscriberFlow.emit(intent)
                } else {
                    Logs.d(TAG, "subscriberPayment: Navigating to Subscriber Payment Fragment")
                    navigateTo(
                        PaymentFragmentDirections.actionPaymentFragmentToSubscriberPaymentFragment(
                            trip.id,
                            null,
                            null,
                            false,
                        )
                    )
                }
            } ?: run {
                Logs.e(TAG, "subscriberPayment: Trip is null, cannot proceed with subscriber payment")
            }
        }
    }

    private suspend fun stopCountdownTimer() {
        Logs.d(TAG, "stopCountdownTimer: Stopping and deleting countdown timer")
        val countdown =
            countdownManagerUseCase.getCountdownById(CountdownId.PAYMENT)?.toModel()
        countdown?.let {
            countdownManagerUseCase.stopCountdown(countdown)
            countdownManagerUseCase.deleteCountdownById(CountdownId.PAYMENT)
            _timerFlow.emit(null)
        }
    }

    private suspend fun insertSubscriberFromDispatch(trip: Trip, dispatchValue: InfoDispatchModel) {
        dispatchValue.subscriber?.let { manualSubscriber ->
            dispatchValue.subscriberUser?.let { manualUser ->
                Subscriber(
                    manualSubscriber = ToolUtils().myAtoi(manualSubscriber),
                    manualUser = ToolUtils().myAtoi(trimUserFromString(manualUser)),
                    tripId = trip.id
                )
            }
        }?.let { subscriber ->
            subscriberUseCase.registerSubscriberAndAssignToTrip(subscriber)
            Logs.d(TAG, "subscriberFromDispatch: Inserted subscriber: $subscriber")
        }
    }

    fun subscriberFromDispatch(trip: Trip?, dispatch: InfoDispatchModel?) {
        viewModelScope.launch(Dispatchers.IO) {
            trip?.let {
                tripUseCase.updateTotalAmount(trip.totalAmount, trip.id)
                stopCountdownTimer()
                Logs.d(TAG, "subscriberFromDispatch: Handling dispatch with clientType: ${dispatch?.clientType}")
                val dispatchValue = dispatch ?: return@launch

                insertSubscriberFromDispatch(it, dispatchValue)

                when (dispatchValue.clientType) {
                    DispatchUseCase.SUBSCRIBER_CREDIT_CARD_COMPULSORY -> {
                        Logs.d(TAG, "subscriberFromDispatch: Navigating to SubscriberPaymentFragment with credit card compulsory")
                        if (!dispatchValue.pin.isNullOrEmpty()) {
                            subscriberPayment(trip)
                        } else {
                            if (dispatchValue.checkCreditLimit) {
                                Logs.d(TAG, "subscriberFromDispatch: Checking credit limit")
                                handleCreditLimit(it)
                            } else {
                                Logs.d(TAG, "subscriberFromDispatch: Proceeding to next step for dispatch subscriber")
                                dispatchSubscriberNextStep(dispatchValue, trip)
                            }
                        }
                    }
                    DispatchUseCase.SUBSCRIBER_TCC -> {
                        Logs.d(TAG, "subscriberFromDispatch: Navigating to TccFragment")
                        navigateTo(
                            PaymentFragmentDirections.actionPaymentFragmentToTccFragment(trip.id)
                        )
                    }
                    else -> {
                        if (dispatchValue.checkCreditLimit) {
                            Logs.d(TAG, "subscriberFromDispatch: Checking credit limit")
                            handleCreditLimit(trip)
                        } else {
                            Logs.d(TAG, "subscriberFromDispatch: Proceeding to next step for dispatch subscriber")
                            dispatchSubscriberNextStep(dispatchValue, trip)
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleCreditLimit(trip: Trip) {
        Logs.d(TAG, "subscriberFromDispatch: Checking credit limit for amount: ${trip.totalAmount}")
        subscriberUseCase.checkCreditLimit(trip.totalAmount, _checkSubscriberCreditLimitFlow)
    }

    fun dispatchSubscriberNextStep(dispatchValue: InfoDispatchModel, trip: Trip) {
        viewModelScope.launch {
            if (dispatchValue.requireSignature) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Requires signature")
                dispatchRequireSignature(dispatchValue)
            } else if (dispatchValue.requireVoucher) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Requires voucher")
                dispatchRequireVoucher(dispatchValue)
            } else if (dispatchValue.requireQr) {
                Logs.d(TAG, "dispatchSubscriberNextStep: Requires QR")
                dispatchRequireQR(dispatchValue)
            } else {
                Logs.d(TAG, "dispatchSubscriberNextStep: Ending trip")
                tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)

                shiftStatusUseCase.getStatus()?.let { status ->
                    Logs.d(TAG, "dispatchSubscriberNextStep: Preparing ticket")
                    ticketUseCase.prepareTicket(
                        trip = trip,
                        dispatch = dispatchValue,
                        shiftStatus = status,
                    )
                }

                showDialog(
                    Pair(
                        CustomDialog.CustomDialogModel(
                            title = context.getString(R.string.operation_authorized),
                            buttons = arrayListOf(ButtonType.ACCEPT),
                            dialogTAG = SUBSCRIBER_RESPONSE_SUCCESS_DIALOG,
                            isCancellable = false,
                        ),
                    ) { response ->

                    }
                )

                Logs.d(TAG, "dispatchSubscriberNextStep: Navigating post payment")
                navigatePostPayment(dispatchValue)
            }
        }
    }

    suspend fun dispatchRequireSignature(dispatch: InfoDispatchModel) {
        navigateTo(PaymentDirections.goToSignatureFragment(dispatch.longDispatchNumber))
    }

    suspend fun dispatchRequireVoucher(dispatch: InfoDispatchModel) {
        navigateTo(PaymentDirections.goToCropImageViewFragment(dispatch.longDispatchNumber))
    }

    suspend fun dispatchRequireQR(dispatch: InfoDispatchModel) {
        navigateTo(PaymentDirections.goToScannerQRFragment(dispatch.longDispatchNumber))
    }

    private suspend fun handleTokenError(flowSumUp: ((Int) -> Unit)?, amount: Int) {
        val callback: (CustomDialog.CustomDialogResponse) -> Unit = { action ->
            if (action.buttonPressed == ButtonType.RETRY) {
                viewModelScope.launch {
                    getToken(flowSumUp, amount)
                }
            } else {
                viewModelScope.launch {
                    _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                }
            }
        }

        Logs.d(TAG, "redSysPaymentState: handleTokenError ERROR popup shown")
        showDialog(
            Pair(
                CustomDialog.CustomDialogModel(
                    title = context.getString(R.string.error_sumup),
                    description = context.getString(R.string.no_response_error),
                    isCancellable = false,
                    buttons = arrayListOf(ButtonType.RETRY, ButtonType.CANCEL),
                    icon = R.drawable.round_warning_24
                ),
                callback
            )
        )
    }

    fun cardPayment(amount: Int, flowSumUp: ((Int) -> Unit)? = null, invoiceRedSys: String? = null) {
        this.invoiceRedSys = invoiceRedSys
        this.amountRedSys = amount
        Logs.d(TAG, "cardPayment: Parameters -> amount = $amount, invoiceRedSys = $invoiceRedSys")

        viewModelScope.launch(Dispatchers.IO) {
            _cardButtonStatusFlow.emit(CustomButton.StyleButton.LOADING)

            try {
                when {
                    haveSumUpConfigured() -> {
                        Logs.d(TAG, "cardPayment: SumUp configuration detected")
                        getToken(flowSumUp, amount)
                    }

                    ingenicoUseCase.cardPaymentAvailable() -> {
                        Logs.d(TAG, "cardPayment: Ingenico card payment available")
                        val intent =
                            ingenicoUseCase.getIngenicoIntentSubscriber(PaymentMethod.CARD, amount)
                        intent?.let {
                            Logs.d(TAG, "cardPayment: Emitting Ingenico intent to flow")
                            _ingenicoCardFlow.emit(it)
                        } ?: run {
                            Logs.e(TAG, "cardPayment: Ingenico intent is null")
                            _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                        }
                    }

                    haveRedSysConfigured() -> {
                        _redSysPaymentStateFlow.value = RedSysPaymentState.PENDING
                        Logs.d(TAG, "cardPayment: RedSys configuration detected")
                        if (haveRedSysUsers()) {
                            Logs.d(TAG, "cardPayment: RedSys users available, starting configuration")
                            redSysUseCase.configureRedSys()
                            val serialNumber =
                                userPreferencesUseCase.getUserPreferences()?.pinPadSerialNumber
                            Logs.d(TAG, "cardPayment: Serial number = $serialNumber")

                            val userRedSys = redSysUseCase.getUsernameRedSys() ?: ""
                            val passRedSys = TemporalData.passwordRedSys
                            Logs.d(TAG, "cardPayment: Attempting RedSys login for user $userRedSys")

                            var loginResponse = TemporalData.redsysLogin

                            if (loginResponse == null || loginResponse is RedSysLoginResponse.Error) {
                                Logs.d(TAG, "cardPayment: No valid RedSys session, performing login")
                                loginResponse = redSysUseCase.loginRedSys(userRedSys, passRedSys)
                                TemporalData.redsysLogin = loginResponse
                            } else {
                                Logs.d(TAG, "cardPayment: Using cached RedSys login")
                            }

                            when (loginResponse) {
                                is RedSysLoginResponse.Success -> {
                                    Logs.d(TAG, "cardPayment: RedSys login successful")
                                    if (serialNumber != null &&
                                        loginResponse.merchantList.isNotEmpty() &&
                                        loginResponse.merchantList[0].terminalList.isNotEmpty()
                                    ) {
                                        if (!redSysUseCase.isPinPadConected()) {
                                            Logs.d(TAG, "cardPayment: Connecting RedSys PinPad")
                                            val terminalData =
                                                loginResponse.merchantList[0].terminalList[0]
                                            redSysSessionState.terminalData = terminalData
                                            redSysUseCase.connectRedSysPinPad(
                                                this@PaymentViewModel,
                                                serialNumber,
                                                terminalData
                                            )
                                        } else {
                                            Logs.d(TAG, "cardPayment: PinPad already connected, proceeding with payment")
                                            amountRedSys?.let { doPaymentRedSys(it, invoiceRedSys) }
                                        }
                                    } else {
                                        Logs.e(TAG, "cardPayment: Merchant or terminal list is empty")
                                        showDialog(
                                            Pair(
                                                CustomDialog.CustomDialogModel(
                                                    title = context.getString(R.string.red_sys_title),
                                                    description = context.getString(R.string.error_red_sys_desc),
                                                    buttons = arrayListOf(ButtonType.ACCEPT)
                                                ),
                                            ) {
                                                viewModelScope.launch(Dispatchers.IO) {
                                                    _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                                                }
                                            }
                                        )
                                    }
                                }

                                is RedSysLoginResponse.Error -> {
                                    Logs.e(TAG, "cardPayment: RedSys login failed -> ${loginResponse.errorMessage}")
                                    val result = RedCLSErrorCodes.getExceptionFromCode(loginResponse.errorCode, null)
                                    Logs.e(TAG, "cardPayment: Error details -> ${result.message}, ${result.msgReturn}")
                                    _redSysPaymentStateFlow.value = RedSysPaymentState.ERROR.also {
                                        it.setErrorCode(loginResponse.errorCode)
                                    }
                                    TemporalData.redsysLogin = redSysUseCase.loginRedSys(userRedSys, passRedSys)
                                }
                        }
                    } else {
                        Logs.d(TAG, "cardPayment: No RedSys users found, navigating to login")
                        _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                        navigateTo(PaymentFragmentDirections.actionPaymentFragmentToLoginRedSysFragment())
                    }
                }

                    else -> {
                        Logs.d(TAG, "cardPayment: No card configuration detected, emitting noCardConfiguredFlow")
                        _noCardConfiguredFlow.emit(true)
                        _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
                    }
                }

            } catch (e: Exception) {
                Logs.e(TAG, "cardPayment: Unexpected error during card payment e = $e")
                _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
            }

            countdownModel?.let { countdown ->
                Logs.d(TAG, "cardPayment: Stopping and deleting countdown")
                countdownManagerUseCase.stopCountdown(countdown)
                countdownManagerUseCase.deleteCountdownById(CountdownId.PAYMENT)
            }
        }
    }

    private suspend fun getToken(flowSumUp: ((Int) -> Unit)?, amount: Int) {
        try {
            sumUpUseCase.getToken().collect {
                if (it != null) {
                    userPreferencesUseCase.insertSumUpTemporalToken(it.accessToken)
                } else {
                    handleTokenError(flowSumUp, amount)
                }
                flowSumUp?.invoke(amount)
            }
        } catch (e: Exception) {
            handleTokenError(flowSumUp, amount)
        }
    }

    private suspend fun getReceipt(
        transactionId: String,
        merchantCode: String,
        temporalToken: String,
    ) {
        try {
            sumUpUseCase.getReceipt(
                idTransaction = transactionId,
                merchantCode = merchantCode,
                token = temporalToken
            ).collect { sumUpReceiptResponse ->
                userPreferencesUseCase.deleteSumUpTemporalToken()
                if (sumUpReceiptResponse != null) {
                    val ticket = sumUpUseCase.prepareSumUpTicket(false, sumUpReceiptResponse)
                    if (ticket != null) {
                        _sumUpTicketFlow.emit(ticket)
                    }
                } else {
                    handleReceiptError(transactionId, merchantCode, temporalToken)
                }
            }
        } catch (e: Exception) {
            handleReceiptError(transactionId, merchantCode, temporalToken)
        }
    }

    private suspend fun handleReceiptError(
        transactionId: String,
        merchantCode: String,
        temporalToken: String,
    ) {
        val dialogCallback: (CustomDialog.CustomDialogResponse) -> Unit =
            { action ->
                if (action.buttonPressed == ButtonType.RETRY) {
                    viewModelScope.launch {
                        getReceipt(transactionId, merchantCode, temporalToken)
                    }
                }
            }

        Logs.d(TAG, "redSysPaymentState: handleReceiptError ERROR popup shown")
        showDialog(
            Pair(
                CustomDialog.CustomDialogModel(
                    title = context.getString(R.string.error_sumup),
                    description = context.getString(R.string.no_response_error),
                    isCancellable = false,
                    buttons = arrayListOf(ButtonType.RETRY),
                    icon = R.drawable.round_warning_24
                ),
                dialogCallback
            )
        )
    }

    private suspend fun doPaymentRedSys(amount: Int, invoice: String?) {
        _redSysStartFlow.emit(Pair(amount, invoice))
    }

    fun uvLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnUVLight()
            delay(8000L)
            _uvLightFlow.emit(false)
        }
    }

    fun turnCourtesyLight() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnCourtesyLight()
        }
    }

    fun turnCourtesyLightOn() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnOnCourtesyLight()
        }
    }

    fun turnCourtesyLightOff() {
        viewModelScope.launch {
            lightSkyGlassUseCase.turnOffCourtesyLight()
        }
    }

    fun checkConditions() {
        viewModelScope.launch {
            val status = Taximeter.getInstance().bluetoothState
            val device = bluetoothLocalUseCase.getLocalBluetooth()

            if (device != null) {
                if ((device.name.startsWith("SKYG"))) {
                    _conditionsLocalFlow.emit(Pair(device, status))
                } else if (device.name.startsWith("SHER")) {
                    _conditionsLocalFlow.emit(Pair(device, status))
                } else {
                    _conditionsLocalFlow.emit(Pair(null, status))
                }
            } else {
                _conditionsLocalFlow.emit(Pair(null, status))
            }
        }
    }

    fun checkIngenicoAuth() {
        viewModelScope.launch {
            Logs.d(TAG, "checkIngenicoAuth: Checking Ingenico auth intent...")
            val intent = ingenicoUseCase.getIngenicoIntentSubscriber(PaymentMethod.SUBSCRIBER)
            if (intent != null) {
                Logs.d(TAG, "checkIngenicoAuth: Ingenico intent received, emitting...")
                _ingenicoSubscriberFlow.emit(intent)
            } else {
                Logs.e(TAG, "checkIngenicoAuth: Ingenico intent is null")
            }
        }
    }

    fun validCredit(trip: Trip?, dispatch: InfoDispatchModel, callback: (Trip) -> Unit) {
        viewModelScope.launch {
            trip?.let {
                withContext(Dispatchers.IO) {
                    tripUseCase.endTrip(trip, PaymentMethod.SUBSCRIBER)
                }

                shiftStatusUseCase.getStatus()?.let { status ->
                    ticketUseCase.prepareTicket(
                        trip = trip,
                        dispatch = dispatch,
                        shiftStatus = status
                    )
                }

                val countdown =
                    countdownManagerUseCase.getCountdownById(CountdownId.PAYMENT)?.toModel()
                countdown?.let {
                    countdownManagerUseCase.stopCountdown(countdown)
                    countdownManagerUseCase.deleteCountdownById(CountdownId.PAYMENT)
                    _timerFlow.emit(null)
                }

                //Navigation
                navigatePostPayment(dispatch)
            }
        }
    }

    fun navigatePostPayment(dispatch: InfoDispatchModel?) {
        viewModelScope.launch {
            val routeId = dispatch?.routeId

            if (!routeId.isNullOrEmpty()) {
                //iTOP siempre va a pago una vez acabados todos los despachos del Multidispatch
                val hasMultiDispatchNotEnded = dispatchUseCase.hasDispatchesNotEnded(routeId)
                if (hasMultiDispatchNotEnded) {
                    _multiDispatchNavigate.emit(true)
                } else {
                    val status =
                        shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                    status.let { statusWithSubStatus ->
                        shiftStatusUseCase.setStatus(statusWithSubStatus, true)
                    }
                }
            } else {
                val status =
                    shiftStatusUseCase.checkForSubStateModifications(ifConstants.STATE_FOR_HIRE)
                status.let { statusWithSubStatus ->
                    shiftStatusUseCase.setStatus(statusWithSubStatus, true)
                }
            }
        }
    }

    fun updateTripAmount(trip: Trip) {
        viewModelScope.launch(Dispatchers.IO) {
            tripUseCase.updateTripAmount(trip)
        }
    }

    suspend fun sumUpGetReceipt(trip: Trip, transactionId: String?) {
        val merchantCode = userPreferencesUseCase.getUserPreferences()?.sumUpMerchantCode
        val temporalToken = userPreferencesUseCase.getSumUpTemporalToken()
        var ticket: String? = null

        if (merchantCode != null && transactionId != null) {
            sumUpUseCase.getReceipt(
                idTransaction = transactionId,
                merchantCode = merchantCode,
                token = temporalToken
            ).collect { sumUpReceiptResponse ->
                userPreferencesUseCase.deleteSumUpTemporalToken()
                if (sumUpReceiptResponse != null) {
                    ticket = sumUpUseCase.prepareSumUpTicket(false, sumUpReceiptResponse)
                    val customerTicket = sumUpUseCase.prepareSumUpTicket(true, sumUpReceiptResponse)


                    if (customerTicket != null) {
                        tripUseCase.setTicketBufferSumUp(trip.id, customerTicket)
                    }
                }
            }
        }

        ticket?.let { _sumUpTicketFlow.emit(it) }
    }

    suspend fun haveSumUpConfigured(): Boolean {
        val userPreferences = userPreferencesUseCase.getUserPreferences()
        return userPreferences?.let {
            it.sumUpMerchantCode.isNotBlank() || it.sumUpMerchantCode.isNotEmpty()
        } ?: false
    }

    suspend fun haveRedSysConfigured(): Boolean {
        val userPreferences = userPreferencesUseCase.getUserPreferences()
        return userPreferences?.let {
            it.pinPadSerialNumber.isNotBlank() || it.pinPadSerialNumber.isNotEmpty()
        } ?: false
    }

    suspend fun haveRedSysUsers(): Boolean {
        val userRedSys = redSysUseCase.getUsernameRedSys()
        val passRedSys = TemporalData.passwordRedSys
        return !userRedSys.isNullOrEmpty() && userRedSys.isNotBlank() && passRedSys.isNotEmpty() && passRedSys.isNotBlank()
    }

    override fun getContext(): Context {
        return context
    }

    override fun conexionPinPadRealizada() {
        Logs.d(TAG, "Interface conexionPinPadRealizada")
        viewModelScope.launch(Dispatchers.IO) {
            amountRedSys?.let { doPaymentRedSys(it, invoiceRedSys) }
        }
    }

    override fun pinPadNoEncontrado() {
        if (_redSysPaymentStateFlow.value != RedSysPaymentState.SUCCESS) {
            viewModelScope.launch(Dispatchers.IO) {
                Logs.d(TAG, "pinPadNoEncontrado")
                _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
            }
        }
    }

    override fun seleccionMonedaPagoDCC(data: RedCLSDccSelectionData): String {
        val redSysDCC = RedSysDCC(
            arg0 = data.originalAmount,
            arg1 = data.originalCurrency,
            arg2 = data.currencyChangeAmount,
            // double changeAmount = data.getCurrencyChangeAmount();
            // String arg2 = String.format(Locale.US, "%4.2f", changeAmount);
            arg3 = data.currencyChangeCode,
            arg4 = data.currencyChangeName,
            arg5 = data.currencyChangeSymbol,
            arg6 = data.percentageCommission,
            arg7 = data.currencyRateWithCommission,
            arg8 = data.currencyRateWithOutCommission,
            arg9 = data.nameEntTermAct,
        )

        redSysSessionState.currentDCC = redSysDCC
        return redSysDCC.arg1 ?: "EUR"
    }

    override fun selectionDeferPayment(p0: RedCLSDeferPaymentData?): String? {
        Logs.d(TAG, "selectionDeferPayment()")
        return null
    }

    fun retrieveDispatchExtraData(id: Long?) {
        viewModelScope.launch {
            id?.let {
                _dispatchExtraDataFlow.emit(dispatchExtraDataDao.getDispatchExtraDataById(id))
            }
        }
    }


    fun useTTSForAmount(
        serviceAmountLocal: Int,
        extraAmountLocal: Int,
        tollAmountLocal: Int,
        tipAmountLocal: Int,
        totalAmountLocal: Int,
    ) {
        viewModelScope.launch {
            var text = ""

            text += context.resources.getString(R.string.tts_service_amount) + " " + serviceAmountLocal.toCurrency() + "\n"

            if (extraAmountLocal != 0) {
                text += context.resources.getString(R.string.tts_extra_amount) + " " + extraAmountLocal.toCurrency() + "\n"
            }

            if (tollAmountLocal != 0) {
                text += context.resources.getString(R.string.tts_toll_amount) + " " + tollAmountLocal.toCurrency() + "\n"
            }

            if (tipAmountLocal != 0) {
                text += context.resources.getString(R.string.tts_tips_amount) + " " + tipAmountLocal.toCurrency() + "\n"
            }

            if ((totalAmountLocal != 0) && (totalAmountLocal != serviceAmountLocal)) {
                text += context.resources.getString(R.string.tts_total_amount) + " " + totalAmountLocal.toCurrency() + "\n"
            }

            ttsUseCase.speakText(text.trim(), Amount)

        }
    }

    fun setUpTTS() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            preferences?.blindLocutionId?.let {
                _showTTSFlow.emit(it)
            }
        }
    }

    fun backToDispatch(idDispatch: Long, trip: Trip?) {
        viewModelScope.launch {
            backToDispatchedUseCase.decreaseBackToDispatched(idDispatch)

            val status = shiftStatusUseCase.getStatus()
            status?.let {
                shiftStatusUseCase.setStatus(ifConstants.STATE_DISPATCHED, status.isManual)
            }

            trip?.let {
                tripUseCase.abandonedTrip(it)
            }

            stopCountdownTimer()

            _backToDispatchedFlow.emit(Pair(null, true))

        }
    }

    fun saveTypedAuthCode(dispatchId: Long, editTextString: String) {
        viewModelScope.launch {
            dispatchUseCase.saveTypedAuthCode(dispatchId, editTextString)
        }
    }

    fun saveTripEndLocation(trip: Trip?) {
        trip?.let {
            viewModelScope.launch(Dispatchers.IO) {
                tripUseCase.saveTripEndLocation(it)
            }
        }
    }

    fun sendPrimeTripSelected(tripType: Int) {
        viewModelScope.launch {
            taximeterManagerUseCase.sendPrimeTripTypeToTaximeter(tripType)
        }
    }

    fun sendSelectedPrimePaymentType(pymentMethod: PaymentMethod) {
        viewModelScope.launch {
            taximeterManagerUseCase.sendSelectedPrimePaymentTypeToTaximeter(pymentMethod.toPaymentPrimeMethod())
        }
    }

    fun askTaximeterPrimeConfig() {
        viewModelScope.launch {
            taximeterManagerUseCase.askPrimeConfiguration()
        }
    }

    fun checkSubscriberAttempts(tripId: Long?) {
        viewModelScope.launch {
            _subscriberAttemptsFlow.emit(subscriberUseCase.getSubscriberAttempts(tripId))
        }
    }

    fun useSubscriberAttempt(trip: Trip?) {
        viewModelScope.launch {
            if (trip?.fromDispatch == true) {
                subscriberUseCase.useSubscriberAttempt()
                _subscriberAttemptsFlow.emit(subscriberUseCase.getSubscriberAttempts(trip.id))
            }
        }
    }

    fun ingenicoAccepted(trip: Trip, dispatch: InfoDispatchModel?, intent: Intent) {
        viewModelScope.launch {
            val ingenicoReceiptData = ingenicoUseCase.getIntentReceiptData(intent)
            ticketUseCase.prepareIngenicoTicket(trip, dispatch, ingenicoReceiptData)

            soundManagerUseCase.playNotification()
        }
    }

    fun ingenicoRejected() {
        viewModelScope.launch {
            _cardButtonStatusFlow.emit(CustomButton.StyleButton.ENABLE)
            soundManagerUseCase.playNotification()
            // tarjetaAppExternaCancelada()
        }
    }

    fun setTicketBufferRedSys(tripId: Long?, clientTicket: String, rtsIdentifier: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "setTicketBufferSumUp: tripId = $tripId, clientTicket = $clientTicket")
            tripId?.let {
                tripUseCase.setTripRtsIdentifier(it, rtsIdentifier)
                tripUseCase.setTicketBufferRedSys(it, clientTicket)
            }
        }
    }


    init {
        viewModelScope.launch {
            isManualService = shiftStatusUseCase.getIsManual() ?: true
        }
    }
}
