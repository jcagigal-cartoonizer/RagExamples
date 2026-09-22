package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import com.interfacom.sdk.taximeter.taximeter.Taximeter
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.PaymentModifiers
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.ALWAYS
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.NOT_ALLOWED
import ifac.td.taxi.domain.model.PaymentModifiers.ModifyImports.ONLY_IMPORT_0
import ifac.td.taxi.domain.model.TTSType
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.TTSUseCase
import ifac.td.taxi.domain.usecase.TTSUseCaseImpl
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.domain.utils.NumberUtils.Companion.toCurrency

import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AddAmountViewModel(
    private val tripUseCase: TripUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val ttsUseCase: TTSUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val _withTaximeterFlow = MutableSharedFlow<Boolean>()
    val withTaximeterFlow = _withTaximeterFlow.asSharedFlow()

    private val TAG = "AddAmountViewModel"
    private var paymentModifiers: PaymentModifiers = PaymentModifiers()

    init {
        viewModelScope.launch {
            paymentModifiers = licensingUseCase.getPaymentModifiers()
        }
    }

    fun updateTrip(
        trip: Trip?,
        serviceAmount: Int,
        tollAmount: Int,
        tipAmount: Int,
        totalAmount: Int,
        extraAmount: Int,
    ) {
        viewModelScope.launch {
            trip?.let {
                it.taximeterAmount = serviceAmount
                it.tolls = tollAmount
                it.tips = tipAmount
                it.totalAmount = totalAmount
                it.extras = extraAmount

                // Si los extras se han puesto a 0 (p.ej. al modificar el precio del taxímetro),
                // reseteamos también los importes crudos del taxímetro (extra1..extra4 y extrasAuto)
                // para que al recargar la pantalla no se vuelvan a sumar y reaparezca el extra.
                if (extraAmount == 0) {
                    it.extra1 = 0
                    it.extra2 = 0
                    it.extra3 = 0
                    it.extra4 = 0
                    it.extrasAuto = 0
                }

                if (trip.addMinimumPriceDispatch && (trip.mininumPriceDispatch != it.taximeterAmount)) {
                    trip.addMinimumPriceDispatch = false
                }

                licensingUseCase.getLicensingParameters()?.minAirportAmount?.let { minAirportAmount ->
                    if (trip.addMinimumPriceAirport && (trip.taximeterAmount != minAirportAmount)) {
                        Logs.d(TAG, "set addMinimumPriceAirport 4: false")
                        trip.addMinimumPriceAirport = false
                    }
                }

                tripUseCase.updateTripAmountsAndExtras(
                    it.id,
                    it.taximeterAmount,
                    it.tolls,
                    it.tips,
                    it.totalAmount,
                    it.extras,
                    it.extra1,
                    it.extra2,
                    it.extra3,
                    it.extra4,
                    it.extrasAuto,
                    it.addMinimumPriceDispatch,
                    it.addMinimumPriceAirport
                )

                navigateBack()
            }
        }
    }

    fun allowsModifyCash(amountIsZero: Boolean, isManual: Boolean, isSubscriber: Boolean, isAmountFromDispatch: Boolean): Boolean {
        if (isAmountFromDispatch)  {
            Logs.d(TAG, "paymentModifiers.isAmountFromDispatch allowsModifyCash false")
            return false
        }

        if (isSubscriber && paymentModifiers.isAllowModifySubscriberAmounts) {
            Logs.d(TAG, "paymentModifiers.isAllowModifySubscriberAmounts allowsModifyCash true")
            return true
        }

        //Sin Protocolo o Manual permitir modificar importes
        val withouProtocol = Taximeter.getInstance().isTaximeterWithoutProtocol
        if (withouProtocol || isManual) {
            Logs.d(TAG, "withoutProtocol $withouProtocol")
            Logs.d(TAG, "isManual $isManual")
            return true
        }

        //TODO: Return False si Tarifa Autocab
        when (paymentModifiers.isAllowModifyCash) {
            NOT_ALLOWED -> {
                Logs.d(TAG, "isAllowModifyCash NOT_ALLOWED: Return False")
                return false
            }
            ONLY_IMPORT_0 -> {
                Logs.d(TAG, "isAllowModifyCash ONLY_IMPORT_0: Return $amountIsZero")
                return amountIsZero
            }
            ALWAYS -> {
                Logs.d(TAG, "isAllowModifyCash ALWAYS: Return true")
                return true
            }
        }

        Logs.d(TAG, "allowsModifyCash return False")
        return false
    }

    fun checkIfWorksWithoutTx() {
        viewModelScope.launch {
            val bluetoothResult = bluetoothLocalUseCase.getLocalBluetooth()
            val value = bluetoothResult == null
            Logs.d(TAG, "worksWithoutTx: $value")
            _withTaximeterFlow.emit(value)
        }
    }


    fun maximumAmountManual(): Int {
        return paymentModifiers.maxManualAmount
    }

    fun allowsTips(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) {
            false
        } else {
            return paymentModifiers.isAllowTips
        }
    }

    fun maximumAmountTips(): Int {
        return paymentModifiers.maxTipAmount
    }

    fun allowsTolls(dispatch: InfoDispatchModel?): Boolean {
        return if ((dispatch?.importeCentral?.toIntOrNull() ?: 0) > 0) {
            false
        } else {
            paymentModifiers.isAllowTolls
        }
    }

    fun maximumAmountTolls(): Int {
        return paymentModifiers.maxTollsAmount
    }

    fun useTTSForAmount(
        serviceAmountLocal: Int,
        extraAmountLocal: Int,
        tollAmountLocal: Int,
        tipAmountLocal: Int,
        totalAmountLocal: Int
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



            when (userPreferencesUseCase.getUserPreferences()?.blindLocutionId) {
                TTSUseCaseImpl.TTSBlindOption.AUTO.value, TTSUseCaseImpl.TTSBlindOption.ONLY_PAYMENT.value -> {
                    ttsUseCase.speakText(text.trim(), TTSType.Amount)
                }
            }
        }
    }

}