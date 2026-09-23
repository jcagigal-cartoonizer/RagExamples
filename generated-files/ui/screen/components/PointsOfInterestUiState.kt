package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PointsOfInterestButtonsState
import ifac.td.taxi.ui.screen.components.PointsOfInterestUiState
import ifac.td.taxi.ui.screen.components.PointsOfInterestUiEffect
import ifac.td.taxi.compose.viewmodel.PointsOfInterestComposeViewModel
import ifac.td.taxi.ui.screen.components.PointsOfInterestDialogState
import ifac.td.taxi.ui.screen.components.PointsOfInterestUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import android.app.Application
data class PointsOfInterestUiState(
    val searchText: String = "",
    val isLoading: Boolean = false,
    val pois: List<Poi> = emptyList(),
    val selectedPoi: Poi? = null,
    val dialog: PointsOfInterestDialogState? = null,
    val buttons: PointsOfInterestButtonsState = PointsOfInterestButtonsState.default(),
)
sealed interface PointsOfInterestUiEvent {
    data class SearchTextChanged(val value: String) : PointsOfInterestUiEvent
    data object SearchClicked : PointsOfInterestUiEvent
    data object CancelClicked : PointsOfInterestUiEvent
    data class PoiClicked(val poi: Poi) : PointsOfInterestUiEvent
    data object DialogDismissed : PointsOfInterestUiEvent
    data object DialogAcceptClicked : PointsOfInterestUiEvent
    data object DialogCancelClicked : PointsOfInterestUiEvent
    data class DialogButtonClicked(val action: PoiDialogAction) : PointsOfInterestUiEvent
}
sealed interface PointsOfInterestUiEffect {
    data object NavigateBack : PointsOfInterestUiEffect
    data class OpenNavigator(val intent: Intent?) : PointsOfInterestUiEffect
    data class ShowPoiDialog(val dialog: PointsOfInterestDialogState) : PointsOfInterestUiEffect
    data class ShowLocateConfirmDialog(val dialog: PointsOfInterestDialogState) : PointsOfInterestUiEffect
    data class SaveHiredZoneAndNavigateBack(val zone: Zone?) : PointsOfInterestUiEffect
    data object HideKeyboard : PointsOfInterestUiEffect
}
enum class PoiDialogAction {
    UBICAR_DESTINO,
    NAVEGAR,
    UBICAR_DESTINO_NAVEGAR
}
data class PointsOfInterestDialogState(
    val title: String,
    val description: String,
    val buttons: List<ButtonTypeUi> = emptyList(),
)
enum class ButtonTypeUi {
    CANCEL,
    ACCEPT,
    UBICAR_DESTINO,
    NAVEGAR,
    UBICAR_DESTINO_NAVEGAR
}
data class PointsOfInterestButtonsState(
    val cancelVisible: Boolean = true,
    val cancelEnabled: Boolean = true,
    val cancelText: String = "Cancel",
    val cancelStyle: ComposeButtonStyle = ComposeButtonStyle.ghost(),
    val searchVisible: Boolean = true,
    val searchEnabled: Boolean = true,
    val searchText: String = "Search",
    val searchStyle: ComposeButtonStyle = ComposeButtonStyle.primary(),
) {
    companion object {
        fun default() = PointsOfInterestButtonsState()
    }
}
data class ComposeButtonStyle(
    val containerColor: Long,
    val contentColor: Long,
    val disabledContainerColor: Long,
    val disabledContentColor: Long,
    val borderColor: Long? = null,
    val cornerRadiusDp: Int = 12,
    val strokeWidthDp: Int = 1
) {
    companion object {
        fun primary() = ComposeButtonStyle(
            containerColor = 0xFF1E88E5,
            contentColor = 0xFFFFFFFF,
            disabledContainerColor = 0xFF90CAF9,
            disabledContentColor = 0xCCFFFFFF
        )
        fun ghost() = ComposeButtonStyle(
            containerColor = 0x00000000,
            contentColor = 0xFF1E88E5,
            disabledContainerColor = 0x00000000,
            disabledContentColor = 0x66000000,
            borderColor = 0xFF1E88E5
        )
        fun danger() = ComposeButtonStyle(
            containerColor = 0xFFD32F2F,
            contentColor = 0xFFFFFFFF,
            disabledContainerColor = 0xFFEF9A9A,
            disabledContentColor = 0xCCFFFFFF
        )
    }
}
class PointsOfInterestComposeViewModel(
    application: Application,
    private val navigatorUseCase: NavigatorUseCase,
    private val locationUseCase: LocationUseCase
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PointsOfInterestUiState())
    val uiState: StateFlow<PointsOfInterestUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<PointsOfInterestUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<PointsOfInterestUiEffect> = _effects.asSharedFlow()
    private val TAG = "PointsOfInterestVM"
    fun onEvent(event: PointsOfInterestUiEvent) {
        when (event) {
            is PointsOfInterestUiEvent.SearchTextChanged -> {
                _uiState.update { it.copy(searchText = event.value) }
            }
            PointsOfInterestUiEvent.SearchClicked -> {
                getPOIs(_uiState.value.searchText)
                sendEffect(PointsOfInterestUiEffect.HideKeyboard)
            }
            PointsOfInterestUiEvent.CancelClicked -> {
                sendEffect(PointsOfInterestUiEffect.NavigateBack)
            }
            is PointsOfInterestUiEvent.PoiClicked -> {
                val poi = event.poi
                val zoneDescription = poi.zone?.nombreZone ?: "Out of zone"
                val streetDescription = poi.street?.let { "\nStreet: $it" } ?: ""
                val description = "\nPoi: $zoneDescription$streetDescription"
                val buttons =
                    if (poi.zone != null) {
                        listOf(
                            ButtonTypeUi.UBICAR_DESTINO,
                            ButtonTypeUi.NAVEGAR,
                            ButtonTypeUi.UBICAR_DESTINO_NAVEGAR
                        )
                    } else {
                        listOf(ButtonTypeUi.NAVEGAR)
                    }
                _uiState.update { it.copy(selectedPoi = poi) }
                sendEffect(
                    PointsOfInterestUiEffect.ShowPoiDialog(
                        PointsOfInterestDialogState(
                            title = poi.poi,
                            description = description,
                            buttons = buttons
                        )
                    )
                )
            }
            PointsOfInterestUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialog = null, selectedPoi = null) }
            }
            PointsOfInterestUiEvent.DialogAcceptClicked -> Unit
            PointsOfInterestUiEvent.DialogCancelClicked -> {
                _uiState.update { it.copy(dialog = null, selectedPoi = null) }
            }
            is PointsOfInterestUiEvent.DialogButtonClicked -> {
                val poi = _uiState.value.selectedPoi ?: return
                when (event.action) {
                    PoiDialogAction.UBICAR_DESTINO -> {
                        sendEffect(
                            PointsOfInterestUiEffect.ShowLocateConfirmDialog(
                                PointsOfInterestDialogState(
                                    title = poi.zone?.nombreZone.orEmpty(),
                                    description = "Do you want to locate destination?",
                                    buttons = listOf(ButtonTypeUi.CANCEL, ButtonTypeUi.ACCEPT)
                                )
                            )
                        )
                    }
                    PoiDialogAction.NAVEGAR -> {
                        openNavigatorApp(poi.lat, poi.lon, poi.street)
                    }
                    PoiDialogAction.UBICAR_DESTINO_NAVEGAR -> {
                        sendEffect(
                            PointsOfInterestUiEffect.ShowLocateConfirmDialog(
                                PointsOfInterestDialogState(
                                    title = poi.zone?.nombreZone.orEmpty(),
                                    description = "Do you want to locate destination?",
                                    buttons = listOf(ButtonTypeUi.CANCEL, ButtonTypeUi.ACCEPT)
                                )
                            )
                        )
                    }
                }
            }
        }
    }
    fun confirmLocateAndMaybeNavigate(navigateAfter: Boolean) {
        val poi = _uiState.value.selectedPoi ?: return
        locateOnHired(poi.zone, navigateAfter)
    }
    fun getPOIs(keyword: String) {
        viewModelScope.launch {
            val poiListener = object : POISListener {
                override fun getPOISSuccess(poiList: ArrayList<Poi>?) {
                    if (poiList != null) {
                        Logs.d(TAG, "getPOISSuccess: $poiList")
                        _uiState.update { it.copy(pois = poiList.toList(), isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                override fun getPOISFailure() {
                    Logs.e(TAG, "Failed getting POI list")
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            _uiState.update { it.copy(isLoading = true) }
            POISDataModule.providePOISPresenter(getApplication(), poiListener).getPois(keyword)
        }
    }
    fun openNavigatorApp(latitude: Double?, longitude: Double?, street: String? = null) {
        viewModelScope.launch(Dispatchers.Main) {
            val navigatorIntent = when {
                latitude != null && longitude != null && !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName("$latitude,$longitude", street)
                latitude != null && longitude != null ->
                    navigatorUseCase.openNavigatorWithCoordinates("$latitude,$longitude")
                !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorApp(street)
                else ->
                    navigatorUseCase.openNavigatorApp()
            }
            sendEffect(PointsOfInterestUiEffect.OpenNavigator(navigatorIntent))
        }
    }
    fun locateOnHired(zone: Zone?, navigateAfter: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            zone?.let { selectedZone ->
                var idMacroZone: Int? = null
                var index = 0
                do {
                    val hasTheZone =
                        W2CLocation.getZoning().macrozones[index].zones.find { z ->
                            selectedZone.idZone == z.idZone && selectedZone.nombreZone == z.nombreZone
                        }
                    hasTheZone?.let {
                        idMacroZone = W2CLocation.getZoning().macrozones[index].idMacrozone
                    }
                    index++
                } while (idMacroZone == null && index < W2CLocation.getZoning().macrozones.size)
                idMacroZone?.let {
                    locationUseCase.locateOnHired(it, selectedZone.idZone)
                    if (navigateAfter) {
                        openNavigatorApp(null, null, null)
                    }
                    sendEffect(PointsOfInterestUiEffect.SaveHiredZoneAndNavigateBack(selectedZone))
                }
            }
        }
    }
    fun sendEffect(effect: PointsOfInterestUiEffect) {
        _effects.tryEmit(effect)
    }
}
