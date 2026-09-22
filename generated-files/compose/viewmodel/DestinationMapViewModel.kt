package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.ui.model.RoutePointModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.getDispatchNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.viewmodel.DestinationMapViewModel
import kotlinx.coroutines.flow.collectLatest
@Composable
fun DestinationMapRouteScreen(
    viewModel: DestinationMapComposeViewModel,
    onLaunchIntent: (Intent?) -> Unit,
    onShowHeader: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        onShowHeader(true)
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is DestinationMapUiEffect.OpenNavigatorIntent -> onLaunchIntent(effect.intent)
                is DestinationMapUiEffect.ShowToast -> Unit
                DestinationMapUiEffect.HideKeyboard -> Unit
            }
        }
    }
    DestinationMapRouteContent(
        uiState = uiState,
        onRoutePointClick = viewModel::onRoutePointClicked,
        onAcceptClick = viewModel::onAcceptClicked,
        onCancelClick = viewModel::onCancelClicked,
        onDismissDialog = viewModel::dismissDialog,
        onConfirmDialog = { coordinates, address ->
            viewModel.confirmOpenNavigator(coordinates, address)
        },
        modifier = modifier
    )
}
@Composable
fun DestinationMapRouteContent(
    uiState: DestinationMapUiState,
    onRoutePointClick: (RoutePointModel) -> Unit,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDialog: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.routePoints) { point ->
                    RoutePointItem(
                        routePoint = point,
                        onClick = { onRoutePointClick(point) }
                    )
                }
            }
            DestinationMapButtons(
                state = uiState.buttonsState,
                onAcceptClick = onAcceptClick,
                onCancelClick = onCancelClick
            )
        }
        when (val dialog = uiState.dialogState) {
            DestinationMapDialogState.Hidden -> Unit
            is DestinationMapDialogState.ConfirmOpenNavigator -> {
                DestinationMapCustomDialog(
                    title = "Open navigator",
                    message = dialog.address?.let {
                        "Do you want to open navigator for:\n$it"
                    } ?: "Do you want to open navigator?",
                    positiveText = "Open",
                    negativeText = "Cancel",
                    onPositiveClick = {
                        onConfirmDialog(dialog.coordinates, dialog.address)
                    },
                    onNegativeClick = onDismissDialog,
                    onDismiss = onDismissDialog
                )
            }
        }
    }
}
@Composable
fun RoutePointItem(
    routePoint: RoutePointModel,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Text(text = routePoint.address)
            Text(text = routePoint.coordinatesTag)
        }
    }
}
