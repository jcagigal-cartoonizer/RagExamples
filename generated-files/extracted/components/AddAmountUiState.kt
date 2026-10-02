package ifac.td.taxi.ui.screen.components
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// # Block 173-2: import android.app.Application
data class AddAmountUiState(
    val trip: Trip? = null,
    val serviceAmountCents: Int = 0,
    val extraAmountCents: Int = 0,
    val tollAmountCents: Int = 0,
    val tipAmountCents: Int = 0,
    val totalAmountCents: Int = 0,
    val originalServiceAmountCents: Int = 0,
    val originalExtraAmountCents: Int = 0,
    val serviceAmountText: String = "",
    val extraAmountText: String = "",
    val tollAmountText: String = "",
    val tipAmountText: String = "",
    val totalAmountText: String = "",
    val serviceAmountHint: String = "",
    val extraAmountHint: String = "",
    val tollAmountHint: String = "",
    val tipAmountHint: String = "",
    val buttonsState: AddAmountButtonsState = AddAmountButtonsState()
)
sealed interface AddAmountUiEvent {
    data class OnTripLoaded(val trip: Trip) : AddAmountUiEvent
    data class OnServiceAmountChanged(val value: String) : AddAmountUiEvent
    data class OnTollAmountChanged(val value: String) : AddAmountUiEvent
    data class OnTipAmountChanged(val value: String) : AddAmountUiEvent
    data object OnFieldDone : AddAmountUiEvent
    data object OnAcceptClicked : AddAmountUiEvent
    data object OnCancelClicked : AddAmountUiEvent
}
sealed interface AddAmountUiEffect {
    data object NavigateBack : AddAmountUiEffect
    data class ShowWarningDialog(val title: String, val description: String) : AddAmountUiEffect
}
data class AddAmountButtonsState(
    val canEditServiceAmount: Boolean = false,
    val showExtras: Boolean = true,
    val showTolls: Boolean = false,
    val showTips: Boolean = false,
    val acceptButton: AddAmountComposeButtonState = AddAmountComposeButtonState(
        text = "Accept",
        type = ButtonType.ACCEPT,
        visible = true,
        enabled = true
    ),
    val cancelButton: AddAmountComposeButtonState = AddAmountComposeButtonState(
        text = "Cancel",
        type = ButtonType.CANCEL,
        visible = true,
        enabled = true
    )
)
data class AddAmountComposeButtonState(
    val text: String,
    val type: ButtonType,
    val visible: Boolean,
    val enabled: Boolean
)
class AddAmountComposeViewModel(
    private val tripUseCase: TripUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val ttsUseCase: TTSUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val TAG = "AddAmountViewModel"
    private val _uiState = MutableStateFlow(AddAmountUiState())
    val uiState: StateFlow<AddAmountUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<AddAmountUiEffect>()
    val uiEffect: SharedFlow<AddAmountUiEffect> = _uiEffect.asSharedFlow()
    private var paymentModifiers: PaymentModifiers = PaymentModifiers()
    init {
        viewModelScope.launch {
            paymentModifiers = licensingUseCase.getPaymentModifiers()
        }
    }
    fun onEvent(event: AddAmountUiEvent) {
        when (event) {
            is AddAmountUiEvent.OnTripLoaded -> setTrip(event.trip)
            is AddAmountUiEvent.OnServiceAmountChanged -> {
                _uiState.update { it.copy(serviceAmountText = event.value) }
                recalculate()
            }
            is AddAmountUiEvent.OnTollAmountChanged -> {
                _uiState.update { it.copy(tollAmountText = event.value) }
                recalculate()
            }
            is AddAmountUiEvent.OnTipAmountChanged -> {
                _uiState.update { it.copy(tipAmountText = event.value) }
                recalculate()
            }
            AddAmountUiEvent.OnFieldDone -> recalculate()
            AddAmountUiEvent.OnCancelClicked -> viewModelScope.launch {
                _uiEffect.emit(AddAmountUiEffect.NavigateBack)
            }
            AddAmountUiEvent.OnAcceptClicked -> collectAmount()
        }
    }
    fun setTrip(trip: Trip) {
        val serviceAmount = trip.taximeterAmount ?: 0
        val extraAmount = trip.extra1 + trip.extra2 + trip.extra3 + trip.extra4 + (trip.extrasAuto ?: 0)
        val tollAmount = trip.tolls ?: 0
        val tipAmount = trip.tips ?: 0
        val totalAmount = trip.totalAmount ?: 0
        val canModify = allowsModifyCash(
            amountIsZero = serviceAmount == 0,
            isManual = trip.taximeterTripId == null,
            isSubscriber = false,
            isAmountFromDispatch = trip.fromDispatch
        )
        val showTolls = allowsTolls(null)
        val showTips = allowsTips(null)
        _uiState.value = _uiState.value.copy(
            trip = trip,
            serviceAmountCents = serviceAmount,
            extraAmountCents = extraAmount,
            tollAmountCents = tollAmount,
            tipAmountCents = tipAmount,
            totalAmountCents = totalAmount,
            originalServiceAmountCents = serviceAmount,
            originalExtraAmountCents = extraAmount,
            serviceAmountText = serviceAmount.toMoneyValue(),
            extraAmountText = extraAmount.toMoneyValue(),
            tollAmountText = tollAmount.toMoneyValue(),
            tipAmountText = tipAmount.toMoneyValue(),
            totalAmountText = totalAmount.toCurrency(),
            serviceAmountHint = serviceAmount.toMoneyValue(),
            extraAmountHint = extraAmount.toMoneyValue(),
            tollAmountHint = tollAmount.toMoneyValue(),
            tipAmountHint = tipAmount.toMoneyValue(),
            buttonsState = AddAmountButtonsState(
                canEditServiceAmount = canModify,
                showExtras = extraAmount != 0,
                showTolls = showTolls,
                showTips = showTips
            )
        )
        useTTSForAmount(serviceAmount, extraAmount, tollAmount, tipAmount, totalAmount)
    }
    fun recalculate() {
        val state = _uiState.value
        val serviceAmount = state.serviceAmountText.toMoneyCents()
        var extraAmount = state.extraAmountCents
        if (state.originalExtraAmountCents != 0) {
            val priceModified = state.buttonsState.canEditServiceAmount &&
                serviceAmount != state.originalServiceAmountCents
            extraAmount = if (priceModified) 0 else state.originalExtraAmountCents
        }
        val tollAmount = state.tollAmountText.toMoneyCents()
        val tipAmount = state.tipAmountText.toMoneyCents()
        val totalAmount = serviceAmount + extraAmount + tollAmount + tipAmount
        _uiState.update {
            it.copy(
                serviceAmountCents = serviceAmount,
                extraAmountCents = extraAmount,
                tollAmountCents = tollAmount,
                tipAmountCents = tipAmount,
                totalAmountCents = totalAmount,
                extraAmountText = extraAmount.toMoneyValue(),
                totalAmountText = totalAmount.toCurrency(),
                buttonsState = it.buttonsState.copy(showExtras = extraAmount != 0)
            )
        }
    }
    fun collectAmount() {
        val state = _uiState.value
        val trip = state.trip ?: return
        val serviceAmount = state.serviceAmountText.toMoneyCents()
        val extraAmount = state.extraAmountCents
        val tollAmount = state.tollAmountCents
        val tipAmount = state.tipAmountCents
        val totalAmount = serviceAmount + extraAmount + tollAmount + tipAmount
        val maxAmountManual = maximumAmountManual()
        val maxAmountTips = maximumAmountTips()
        val maxAmountTolls = maximumAmountTolls()
        if (maxAmountManual > 0 && ((serviceAmount - (trip.taximeterAmount ?: 0)) > maxAmountManual)) {
            emitWarning(R.string.warning, R.string.toastMaxService)
            return
        }
        if (maxAmountTips > 0 && ((tipAmount - (trip.tips ?: 0)) > maxAmountTips)) {
            emitWarning(R.string.warning, R.string.toastMaxTips)
            return
        }
        if (maxAmountTolls > 0 && ((tollAmount - (trip.tolls ?: 0)) > maxAmountTolls)) {
            emitWarning(R.string.warning, R.string.toastMaxTolls)
            return
        }
        viewModelScope.launch {
            tripUseCase.updateTripAmountsAndExtras(
                trip.id,
                serviceAmount,
                tollAmount,
                tipAmount,
                totalAmount,
                extraAmount,
                trip.extra1,
                trip.extra2,
                trip.extra3,
                trip.extra4,
                trip.extrasAuto,
                trip.addMinimumPriceDispatch,
                trip.addMinimumPriceAirport
            )
            _uiEffect.emit(AddAmountUiEffect.NavigateBack)
        }
    }
    fun emitWarning(titleRes: Int, descRes: Int) {
        viewModelScope.launch {
            _uiEffect.emit(
                AddAmountUiEffect.ShowWarningDialog(
                    title = getApplication<Application>().getString(titleRes),
                    description = getApplication<Application>().getString(descRes)
                )
            )
        }
    }
    fun allowsModifyCash(
        amountIsZero: Boolean,
        isManual: Boolean,
        isSubscriber: Boolean,
        isAmountFromDispatch: Boolean
    ): Boolean {
        if (isAmountFromDispatch) return false
        if (isSubscriber && paymentModifiers.isAllowModifySubscriberAmounts) return true
        val withoutProtocol = Taximeter.getInstance().isTaximeterWithoutProtocol
        if (withoutProtocol || isManual) return true
        return when (paymentModifiers.isAllowModifyCash) {
            NOT_ALLOWED -> false
            ONLY_IMPORT_0 -> amountIsZero
            ALWAYS -> true
            else -> false
        }
    }
    fun allowsTips(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) false else paymentModifiers.isAllowTips
    }
    fun allowsTolls(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) false else paymentModifiers.isAllowTolls
    }
    fun maximumAmountManual(): Int = paymentModifiers.maxManualAmount
    fun maximumAmountTips(): Int = paymentModifiers.maxTipAmount
    fun maximumAmountTolls(): Int = paymentModifiers.maxTollsAmount
    fun useTTSForAmount(
        serviceAmountLocal: Int,
        extraAmountLocal: Int,
        tollAmountLocal: Int,
        tipAmountLocal: Int,
        totalAmountLocal: Int
    ) {
        viewModelScope.launch {
            var text = ""
            text += getApplication<Application>().getString(R.string.tts_service_amount) + " " + serviceAmountLocal.toCurrency() + "\n"
            if (extraAmountLocal != 0) text += getApplication<Application>().getString(R.string.tts_extra_amount) + " " + extraAmountLocal.toCurrency() + "\n"
            if (tollAmountLocal != 0) text += getApplication<Application>().getString(R.string.tts_toll_amount) + " " + tollAmountLocal.toCurrency() + "\n"
            if (tipAmountLocal != 0) text += getApplication<Application>().getString(R.string.tts_tips_amount) + " " + tipAmountLocal.toCurrency() + "\n"
            if (totalAmountLocal != 0 && totalAmountLocal != serviceAmountLocal) text += getApplication<Application>().getString(R.string.tts_total_amount) + " " + totalAmountLocal.toCurrency() + "\n"
            when (userPreferencesUseCase.getUserPreferences()?.blindLocutionId) {
                TTSUseCaseImpl.TTSBlindOption.AUTO.value,
                TTSUseCaseImpl.TTSBlindOption.ONLY_PAYMENT.value -> ttsUseCase.speakText(text.trim(), TTSType.Amount)
            }
        }
    }
}
fun String.toMoneyCents(): Int {
    return this.replace(".", "")
        .replace(",", "")
        .replace(Regex("[^0-9-]"), "")
        .toIntOrNull() ?: 0
}
