package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.DestinationMapButtonsState
import ifac.td.taxi.ui.screen.components.DestinationMapUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 71-2: import android.app.Application
class DestinationMapComposeViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val navigatorUseCase: NavigatorUseCase,
    application: Application
) : AndroidViewModel(application) {
    private val TAG = "DestinationMapViewModel"
    private val _uiState = MutableStateFlow(
        DestinationMapUiState(
            buttonsState = DestinationMapButtonsState.defaultForDestinationMap()
        )
    )
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<DestinationMapUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    fun onDispatchesChanged(arDispatches: List<Any>?) {
        val routePoints = mutableListOf<RoutePointModel>()
        val dispatchColorMap = mutableMapOf<String, Int>()
        var colorIndex = 0
        val arrColors = intArrayOf(
            getApplication<Application>().getColor(R.color.routes_one),
            getApplication<Application>().getColor(R.color.grey_divider),
            getApplication<Application>().getColor(R.color.switch_thumb_on),
            getApplication<Application>().getColor(R.color.routes_four)
        )
        arDispatches?.forEach { item ->
            val infoDispatchModel = item
            val dispatchNumber = (infoDispatchModel as? HasDispatchFields)?.getDispatchNumber() ?: "Default"
            val color = dispatchColorMap.getOrPut(dispatchNumber) {
                val assigned = arrColors[colorIndex % arrColors.size]
                colorIndex++
                assigned
            }
            val pickUpAddress = infoDispatchModel.pickUpAdress ?: ""
            val pickUpCoordinates = infoDispatchModel.pickUpCoordinates ?: ""
            if (pickUpAddress.isNotEmpty()) {
                routePoints.add(
                    RoutePointModel(
                        type = RoutePointModel.RoutePointType.PICK_UP,
                        address = pickUpAddress,
                        coordinatesTag = pickUpCoordinates,
                        order = infoDispatchModel.orderPickUp,
                        dispatchNumber = infoDispatchModel.getDispatchNumber(),
                        titleColor = color
                    )
                )
            }
            val destinyAddressList = infoDispatchModel.destinyAdress ?: emptyList()
            val destinyCoordinatesList = infoDispatchModel.dropOffCoordinates ?: emptyList()
            if (destinyAddressList.size == destinyCoordinatesList.size) {
                destinyAddressList.forEachIndexed { index, address ->
                    routePoints.add(
                        RoutePointModel(
                            type = RoutePointModel.RoutePointType.DROP_OFF,
                            address = address,
                            coordinatesTag = destinyCoordinatesList[index],
                            order = infoDispatchModel.orderDropOff,
                            dispatchNumber = infoDispatchModel.getDispatchNumber(),
                            titleColor = color
                        )
                    )
                }
            }
        }
        _uiState.update {
            it.copy(routePoints = routePoints)
        }
    }
    fun onRoutePointClicked(routePoint: RoutePointModel) {
        _uiState.update {
            it.copy(
                dialogState = DestinationMapDialogState.ConfirmOpenNavigator(
                    coordinates = routePoint.coordinatesTag,
                    address = routePoint.address
                )
            )
        }
    }
    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = DestinationMapDialogState.Hidden) }
    }
    fun confirmOpenNavigator(coordinates: String, street: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "confirmOpenNavigator: Coordinates = $coordinates, Street = $street")
            val navigatorIntent = when {
                coordinates.isNotEmpty() && !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName(coordinates, street)
                coordinates.isNotEmpty() ->
                    navigatorUseCase.openNavigatorWithCoordinates(coordinates)
                !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorApp(street)
                else ->
                    navigatorUseCase.openNavigatorApp()
            }
            _uiEffect.emit(DestinationMapUiEffect.OpenNavigatorIntent(navigatorIntent))
            dismissDialog()
        }
    }
    fun setButtonsState(state: DestinationMapButtonsState) {
        _uiState.update { it.copy(buttonsState = state) }
    }
    fun onAcceptClicked() {
        // Replace fragment button logic here.
        // Example: open navigator if a selected point exists, or show message.
    }
    fun onCancelClicked() {
        viewModelScope.launch {
            _uiEffect.emit(DestinationMapUiEffect.HideKeyboard)
        }
    }
}
interface HasDispatchFields {
    fun getDispatchNumber(): String?
    val pickUpAdress: String?
    val pickUpCoordinates: String?
    val destinyAdress: List<String>?
    val dropOffCoordinates: List<String>?
    val orderPickUp: Int?
    val orderDropOff: Int?
}
