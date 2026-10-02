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
// # Block 4-1: import android.app.Application
data class FixedPriceUiState(
    val isLoadingSuggestions: Boolean = false,
    val suggestions: List<SuggestModel> = emptyList(),
    val selectedDropOff: SuggestModel? = null,
    val fixedPrice: FixedPricePriceState = FixedPricePriceState(),
    val dialogState: FixedPriceDialogState? = null,
    val isSuggestionListVisible: Boolean = false,
    val isBottomSheetExpanded: Boolean = false,
    val mapTarget: FixedPriceMapTarget? = null,
    val isMapReady: Boolean = false,
)
data class FixedPricePriceState(
    val taxiPrice: String? = null,
    val vanPrice: String? = null,
    val businessPrice: String? = null,
    val taxiError: Boolean = false,
    val vanError: Boolean = false,
    val businessError: Boolean = false,
    val showTaxiLoading: Boolean = true,
    val showVanLoading: Boolean = true,
    val showBusinessLoading: Boolean = true,
)
sealed class FixedPriceMapTarget {
    data class Pickup(
        val pickup: LatLng,
    ) : FixedPriceMapTarget()
    data class DropOff(
        val dropOff: LatLng,
        val pickup: LatLng,
        val moveCamera: Boolean = true,
    ) : FixedPriceMapTarget()
}
data class FixedPriceDialogState(
    val title: String,
    val message: String,
    val positiveText: String,
    val negativeText: String? = null,
    val dismissible: Boolean = true,
)
sealed class FixedPriceUiEvent {
    data class SearchChanged(val text: String, val isPoiSearch: Boolean) : FixedPriceUiEvent()
    data class SuggestionSelected(val item: SuggestModel?) : FixedPriceUiEvent()
    data object CloseDropOff : FixedPriceUiEvent()
    data object ResetPrices : FixedPriceUiEvent()
    data object HideDialog : FixedPriceUiEvent()
    data object MapReady : FixedPriceUiEvent()
    data class DispatchLoaded(val info: InfoDispatchModel?) : FixedPriceUiEvent()
}
sealed class FixedPriceUiEffect {
    data class NavigateBack(val withResult: Boolean = false) : FixedPriceUiEffect()
    data class ShowToast(val message: String) : FixedPriceUiEffect()
    data class ShowDialog(val dialog: FixedPriceDialogState) : FixedPriceUiEffect()
    data class HideKeyboard(val force: Boolean = true) : FixedPriceUiEffect()
    data class MoveMapToPickup(val pickup: LatLng) : FixedPriceUiEffect()
    data class MoveMapToDropOff(val dropOff: LatLng, val pickup: LatLng, val moveCamera: Boolean) : FixedPriceUiEffect()
}
class FixedPriceComposeViewModel(
    private val locationUseCase: LocationUseCase,
    private val fixedPriceUseCase: FixedPriceUseCase,
    private val cercaliaUseCase: BravoRestApiUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val context: Application,
) : BaseViewModel(context) {
    private val TAG = "FixedPriceComposeViewModel"
    private val _uiState = MutableStateFlow(FixedPriceUiState())
    val uiState: StateFlow<FixedPriceUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<FixedPriceUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<FixedPriceUiEffect> = _uiEffect.asSharedFlow()
    var marker: Marker? = null
    private var lastRequestedTrip: Trip? = null
    private val poiListener = object : POISListener {
        override fun getPOISFailure() {
            Logs.e(TAG, "getPOISFailure")
        }
        override fun getPOISSuccess(data: ArrayList<Poi>?) {
            viewModelScope.launch {
                val suggestions = data?.map {
                    SuggestModel(
                        it.poi,
                        it.locality,
                        it.municipalyity,
                        it.province,
                        it.street,
                        it.coord,
                        null,
                        0.0f,
                    )
                }.orEmpty()
                _uiState.update {
                    it.copy(
                        suggestions = suggestions.sortedByDescending { item -> item.score },
                        isLoadingSuggestions = false,
                        isSuggestionListVisible = suggestions.isNotEmpty()
                    )
                }
            }
        }
    }
    private val callback: suspend (List<SuggestModel>) -> Unit = { list ->
        _uiState.update {
            it.copy(
                suggestions = list.sortedByDescending { item -> item.score },
                isLoadingSuggestions = false,
                isSuggestionListVisible = list.isNotEmpty()
            )
        }
    }
    private val fixedPriceCallback = object : FixedPriceCallback {
        override fun onCompleted(response: PriceEstimationDTO?) {
            viewModelScope.launch {
                if (response != null) {
                    lastRequestedTrip?.let { trip ->
                        fixedPriceUseCase.saveFixedPriceData(trip, response)
                    }
                    val taxi = response.prices.firstOrNull { it.serviceType == "taxi" }?.finalPrice?.toString()
                    val van = response.prices.firstOrNull { it.serviceType == "largeCar" }?.finalPrice?.toString()
                    val business = response.prices.firstOrNull { it.serviceType == "business" }?.finalPrice?.toString()
                    _uiState.update {
                        it.copy(
                            fixedPrice = it.fixedPrice.copy(
                                taxiPrice = taxi,
                                vanPrice = van,
                                businessPrice = business,
                                taxiError = taxi == null,
                                vanError = van == null,
                                businessError = business == null,
                                showTaxiLoading = false,
                                showVanLoading = false,
                                showBusinessLoading = false,
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            fixedPrice = it.fixedPrice.copy(
                                taxiError = true,
                                vanError = true,
                                businessError = true,
                                showTaxiLoading = false,
                                showVanLoading = false,
                                showBusinessLoading = false,
                            )
                        )
                    }
                }
            }
        }
        override fun onError(p0: String?) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        fixedPrice = it.fixedPrice.copy(
                            taxiError = true,
                            vanError = true,
                            businessError = true,
                            showTaxiLoading = false,
                            showVanLoading = false,
                            showBusinessLoading = false,
                        )
                    )
                }
                _uiEffect.emit(FixedPriceUiEffect.ShowToast(p0 ?: "Error"))
            }
        }
    }
    private val localCallback: (Triple<Double?, Double?, Double?>) -> Unit = { data ->
        viewModelScope.launch {
            val taxi = data.first?.toString()
            val van = data.second?.toString()
            val business = data.third?.toString()
            _uiState.update {
                it.copy(
                    fixedPrice = it.fixedPrice.copy(
                        taxiPrice = taxi,
                        vanPrice = van,
                        businessPrice = business,
                        taxiError = taxi == null,
                        vanError = van == null,
                        businessError = business == null,
                        showTaxiLoading = false,
                        showVanLoading = false,
                        showBusinessLoading = false,
                    )
                )
            }
        }
    }
    fun onEvent(event: FixedPriceUiEvent) {
        when (event) {
            is FixedPriceUiEvent.SearchChanged -> suggestAddresses(event.text, event.isPoiSearch)
            is FixedPriceUiEvent.SuggestionSelected -> handleSelectedDropOff(event.item)
            FixedPriceUiEvent.CloseDropOff -> updateSelectedDropOff(null)
            FixedPriceUiEvent.ResetPrices -> resetFixedPriceData()
            FixedPriceUiEvent.HideDialog -> _uiState.update { it.copy(dialogState = null) }
            FixedPriceUiEvent.MapReady -> _uiState.update { it.copy(isMapReady = true) }
            is FixedPriceUiEvent.DispatchLoaded -> getDispatchDestinationCoordinates(event.info)
        }
    }
    fun suggestAddresses(text: String, isPoiSearch: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingSuggestions = true) }
            if (isPoiSearch) {
                POISDataModule.providePOISPresenter(context, poiListener).getPois(text)
            } else {
                cercaliaUseCase.suggest(text, callback)
            }
        }
    }
    fun updateSelectedDropOff(itemClick: SuggestModel?) {
        _uiState.update { it.copy(selectedDropOff = itemClick) }
        if (itemClick == null) {
            _uiState.update { it.copy(dialogState = null) }
        }
    }
    fun handleSelectedDropOff(selectedModel: SuggestModel?) {
        updateSelectedDropOff(selectedModel)
        if (selectedModel == null) {
            resetFixedPriceData()
            return
        }
        val coords = getCoordinatesFromString(selectedModel.coord)
        if (coords != null) {
            _uiState.update { it.copy(dialogState = null) }
            viewModelScope.launch {
                _uiEffect.emit(FixedPriceUiEffect.HideKeyboard())
            }
            sendFixedPriceRequest(selectedModel)
        }
    }
    fun getCoordinatesFromString(input: String?): LatLng? {
        if (input == null) return null
        return runCatching {
            val data = input.split(",")
            LatLng(data[0], data[1])
        }.getOrNull()
    }
    fun sendFixedPriceRequest(dropOffData: SuggestModel? = null, trip: Trip? = null, dispatch: InfoDispatchModel? = null) {
        if (trip == null) {
            dropOffData?.let { fixedPriceUseCase.getFixedPriceInVacant(it, fixedPriceCallback, localCallback) }
            return
        }
        lastRequestedTrip = trip
        when {
            dispatch != null -> fixedPriceUseCase.getFixedPriceFromDispatch(trip, dispatch, dropOffData, fixedPriceCallback, localCallback)
            dropOffData != null -> fixedPriceUseCase.getFixedPriceFromTrip(trip, dropOffData, fixedPriceCallback, localCallback)
        }
    }
    fun getDispatchDestinationCoordinates(value: InfoDispatchModel?) {
        viewModelScope.launch {
            value?.let {
                val dispatchWithAddresses = dispatchUseCase.getDispatchWithAddressById(value.id)
                dispatchWithAddresses?.let { data ->
                    val lastDestination = data.addresses?.findLast { !it.isPickup }
                    val pickUp = data.addresses?.find { it.isPickup }
                    if (lastDestination != null) {
                        val coordinates = CoordenatesUtil.parseCoordinatesFromLatLong(lastDestination.coordenadas ?: "")
                        val pickUpCoordinates = CoordenatesUtil.parseCoordinatesFromLatLong(pickUp?.coordenadas ?: "")
                        if (coordinates != null && pickUpCoordinates != null) {
                            _uiState.update {
                                it.copy(
                                    mapTarget = FixedPriceMapTarget.DropOff(
                                        dropOff = LatLng(coordinates.Lat, coordinates.Lng),
                                        pickup = LatLng(pickUpCoordinates.Lat, pickUpCoordinates.Lng),
                                        moveCamera = true
                                    )
                                )
                            }
                        }
                    } else if (pickUp != null) {
                        val pickUpCoordinates = CoordenatesUtil.parseCoordinatesFromLatLong(pickUp.coordenadas ?: "")
                        if (pickUpCoordinates != null) {
                            _uiState.update {
                                it.copy(mapTarget = FixedPriceMapTarget.Pickup(LatLng(pickUpCoordinates.Lat, pickUpCoordinates.Lng)))
                            }
                        }
                    }
                }
            }
        }
    }
    fun resetFixedPriceData() {
        _uiState.update {
            it.copy(
                fixedPrice = FixedPricePriceState(
                    taxiPrice = null,
                    vanPrice = null,
                    businessPrice = null,
                    taxiError = false,
                    vanError = false,
                    businessError = false,
                    showTaxiLoading = true,
                    showVanLoading = true,
                    showBusinessLoading = true,
                ),
                suggestions = emptyList(),
                isSuggestionListVisible = false,
                selectedDropOff = null,
                dialogState = null
            )
        }
    }
}
