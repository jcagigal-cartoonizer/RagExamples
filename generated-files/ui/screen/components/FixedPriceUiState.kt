package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.rest.poi.response.Poi
import com.interfacom.sdk.taximeter.bravocomm.rest.tsab_rest.FixedPriceCallback
import com.interfacom.sdk.taximeter.bravocomm.rest.tsab_rest.fixed_price.PriceEstimationDTO
import com.interfacom.sdk.taximeter.licensing.rest.pois.POISDataModule
import com.interfacom.sdk.taximeter.licensing.rest.pois.POISListener
import com.nexusgeographics.cercalia.maps.features.Marker
import com.nexusgeographics.cercalia.maps.model.LatLng
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.BravoRestApiUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.FixedPriceUseCase
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.rest.bravoRest.SuggestModel
import ifac.td.taxi.ui.util.CoordenatesUtil
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
data class FixedPriceUiState(
    val query: String = "",
    val isPoiSearch: Boolean = false,
    val suggestions: List<SuggestModel> = emptyList(),
    val selectedDropOff: SuggestModel? = null,
    val isSuggestionsVisible: Boolean = false,
    val isBottomSheetExpanded: Boolean = false,
    val isDropOffLocked: Boolean = false,
    val pickupLatLng: LatLng? = null,
    val dropOffLatLng: LatLng? = null,
    val priceTaxi: String = "",
    val priceVan: String = "",
    val priceBusiness: String = "",
    val isTaxiLoading: Boolean = true,
    val isVanLoading: Boolean = true,
    val isBusinessLoading: Boolean = true,
    val isTaxiError: Boolean = false,
    val isVanError: Boolean = false,
    val isBusinessError: Boolean = false,
    val showCloseButton: Boolean = true,
    val showSuggestions: Boolean = true,
)
sealed interface FixedPriceUiEvent {
    data class QueryChanged(val query: String) : FixedPriceUiEvent
    data class PoiSearchChanged(val enabled: Boolean) : FixedPriceUiEvent
    data object CloseClicked : FixedPriceUiEvent
    data class SuggestionClicked(val item: SuggestModel) : FixedPriceUiEvent
    data object BottomSheetHidden : FixedPriceUiEvent
    data object ClearSelection : FixedPriceUiEvent
    data object RetryPrices : FixedPriceUiEvent
}
sealed interface FixedPriceUiEffect {
    data class ShowToast(val message: String) : FixedPriceUiEffect
    data class UpdateMapPickup(val latLng: LatLng, val moveCamera: Boolean = true) : FixedPriceUiEffect
    data class UpdateMapDropOff(val latLng: LatLng, val pickupLatLng: LatLng?, val moveCamera: Boolean = true) : FixedPriceUiEffect
    data class OpenDialog(val dialog: FixedPriceDialogState) : FixedPriceUiEffect
    data object CloseDialog : FixedPriceUiEffect
    data object HideKeyboard : FixedPriceUiEffect
    data object NavigateBack : FixedPriceUiEffect
}
data class FixedPriceDialogState(
    val title: String,
    val message: String,
    val positiveText: String,
    val negativeText: String? = null,
)
class FixedPriceComposeViewModel(
    private val locationUseCase: LocationUseCase,
    private val fixedPriceUseCase: FixedPriceUseCase,
    private val cercaliaUseCase: BravoRestApiUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val context: Application,
) : AndroidViewModel(context) {
    private val TAG = "FixedPriceComposeViewModel"
    private val _uiState = MutableStateFlow(FixedPriceUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<FixedPriceUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    private var lastRequestedTrip: Trip? = null
    var marker: Marker? = null
    private val poiListener = object : POISListener {
        override fun getPOISFailure() {
            Logs.e(TAG, "getPOISFailure")
        }
        override fun getPOISSuccess(data: ArrayList<Poi>?) {
            viewModelScope.launch {
                val list = data.orEmpty().map {
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
                }
                _uiState.update {
                    it.copy(
                        suggestions = list.sortedByDescending { s -> s.score },
                        isSuggestionsVisible = list.isNotEmpty()
                    )
                }
            }
        }
    }
    private val callback: suspend (List<SuggestModel>) -> Unit = { list ->
        _uiState.update {
            it.copy(
                suggestions = list.sortedByDescending { s -> s.score },
                isSuggestionsVisible = list.isNotEmpty()
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
                    var taxi = _uiState.value.priceTaxi
                    var van = _uiState.value.priceVan
                    var business = _uiState.value.priceBusiness
                    response.prices.forEach { price ->
                        when (price.serviceType) {
                            "taxi" -> taxi = price.finalPrice.toString()
                            "largeCar" -> van = price.finalPrice.toString()
                            "business" -> business = price.finalPrice.toString()
                        }
                    }
                    _uiState.update {
                        it.copy(
                            priceTaxi = taxi,
                            priceVan = van,
                            priceBusiness = business,
                            isTaxiLoading = false,
                            isVanLoading = false,
                            isBusinessLoading = false,
                            isTaxiError = false,
                            isVanError = false,
                            isBusinessError = false,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTaxiError = true,
                            isVanError = true,
                            isBusinessError = true,
                            isTaxiLoading = false,
                            isVanLoading = false,
                            isBusinessLoading = false,
                        )
                    }
                    _uiEffect.emit(FixedPriceUiEffect.ShowToast("Error obteniendo precio fijo"))
                }
            }
        }
        override fun onError(p0: String?) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isTaxiError = true,
                        isVanError = true,
                        isBusinessError = true,
                        isTaxiLoading = false,
                        isVanLoading = false,
                        isBusinessLoading = false,
                    )
                }
                _uiEffect.emit(FixedPriceUiEffect.ShowToast(p0 ?: "Error obteniendo precio fijo"))
            }
        }
    }
    private val localCallback: (Triple<Double?, Double?, Double?>) -> Unit = { data ->
        viewModelScope.launch {
            val taxi = data.first?.toString().orEmpty()
            val van = data.second?.toString().orEmpty()
            val business = data.third?.toString().orEmpty()
            _uiState.update {
                it.copy(
                    priceTaxi = taxi,
                    priceVan = van,
                    priceBusiness = business,
                    isTaxiLoading = false,
                    isVanLoading = false,
                    isBusinessLoading = false
                )
            }
        }
    }
    fun onEvent(event: FixedPriceUiEvent) {
        when (event) {
            is FixedPriceUiEvent.QueryChanged -> {
                _uiState.update { it.copy(query = event.query) }
                if (event.query.length < 3) {
                    _uiState.update { it.copy(suggestions = emptyList(), isSuggestionsVisible = false) }
                    return
                }
                suggestAddresses(event.query, _uiState.value.isPoiSearch)
            }
            is FixedPriceUiEvent.PoiSearchChanged -> {
                _uiState.update { it.copy(isPoiSearch = event.enabled) }
            }
            FixedPriceUiEvent.CloseClicked -> {
                clearSelection()
            }
            is FixedPriceUiEvent.SuggestionClicked -> {
                selectSuggestion(event.item)
            }
            FixedPriceUiEvent.BottomSheetHidden -> {
                clearSelection()
            }
            FixedPriceUiEvent.ClearSelection -> clearSelection()
            FixedPriceUiEvent.RetryPrices -> {
                _uiState.update {
                    it.copy(
                        isTaxiLoading = true,
                        isVanLoading = true,
                        isBusinessLoading = true,
                        isTaxiError = false,
                        isVanError = false,
                        isBusinessError = false
                    )
                }
                _uiState.value.selectedDropOff?.let { requestFixedPrice(it) }
            }
        }
    }
    fun suggestAddresses(text: String, isPoiSearch: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isPoiSearch) {
                POISDataModule.providePOISPresenter(context, poiListener).getPois(text)
            } else {
                cercaliaUseCase.suggest(text) { list -> viewModelScope.launch { callback(list) } }
            }
        }
    }
    fun selectSuggestion(item: SuggestModel) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedDropOff = item,
                    query = item.getPrintableStreetTextShort(),
                    isSuggestionsVisible = false,
                    isBottomSheetExpanded = true
                )
            }
            _uiEffect.emit(FixedPriceUiEffect.HideKeyboard)
            requestFixedPrice(item)
        }
    }
    fun clearSelection() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedDropOff = null,
                    query = "",
                    suggestions = emptyList(),
                    isSuggestionsVisible = false,
                    isBottomSheetExpanded = false,
                    dropOffLatLng = null,
                    priceTaxi = "",
                    priceVan = "",
                    priceBusiness = "",
                    isTaxiLoading = true,
                    isVanLoading = true,
                    isBusinessLoading = true,
                    isTaxiError = false,
                    isVanError = false,
                    isBusinessError = false
                )
            }
            marker = null
            _uiEffect.emit(FixedPriceUiEffect.CloseDialog)
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
                        val pick = CoordenatesUtil.parseCoordinatesFromLatLong(pickUp?.coordenadas ?: "")
                        if (coordinates != null && pick != null) {
                            _uiEffect.emit(
                                FixedPriceUiEffect.UpdateMapDropOff(
                                    LatLng(coordinates.Lat!!, coordinates.Lng!!),
                                    LatLng(pick.Lat!!, pick.Lng!!),
                                    true
                                )
                            )
                        }
                    } else {
                        val pick = CoordenatesUtil.parseCoordinatesFromLatLong(pickUp?.coordenadas ?: "")
                        if (pick != null) {
                            _uiEffect.emit(
                                FixedPriceUiEffect.UpdateMapPickup(
                                    LatLng(pick.Lat!!, pick.Lng!!),
                                    true
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    fun requestFixedPrice(suggest: SuggestModel) {
        val trip = lastRequestedTrip
        if (trip == null) {
            fixedPriceUseCase.getFixedPriceInVacant(suggest, fixedPriceCallback, localCallback)
        } else {
            fixedPriceUseCase.getFixedPriceFromTrip(trip, suggest, fixedPriceCallback, localCallback)
        }
    }
}
