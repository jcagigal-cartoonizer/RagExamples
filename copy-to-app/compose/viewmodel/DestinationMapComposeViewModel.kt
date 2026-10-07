package ifac.td.taxi.compose.viewmodel
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
import ifac.td.taxi.viewmodel.DestinationMapViewModel
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.DestinationMapScreen
// # Block 121-2: import android.app.Application
class DestinationMapComposeViewModel(
    private val dispatchUseCase: DispatchUseCase,
    private val navigatorUseCase: NavigatorUseCase,
    context: Application,
) : AndroidViewModel(context) {
    private val TAG = "DestinationMapViewModel"
    lateinit var buttonsState : DestinationMapButtonsState
    lateinit var dialogState : DestinationMapDialogState
    private val _uiState = MutableStateFlow(DestinationMapUiState())
    val uiState: StateFlow<DestinationMapUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<DestinationMapUiEffect>()
    val uiEffect: SharedFlow<DestinationMapUiEffect> = _uiEffect.asSharedFlow()
    init {
        dialogState = _uiState.value.dialog
        observeDispatches()
    }
    fun observeDispatches() {
        viewModelScope.launch {
            dispatchUseCase.observeMultiDispatch() // adapt this to your real source
                .collect { arDispatches ->
                    Logs.d(TAG, "observeDispatches: collected dispatches size=${arDispatches?.size ?: 0}")
                    val routePoints = buildRoutePoints(arDispatches)
                    _uiState.update { old ->
                        old.copy(
                            routePoints = routePoints,
                            buttonsState = old.buttonsState.copy(
                                isRouteVisible = routePoints.isNotEmpty()
                            )
                        )
                    }
                }
        }
    }
    fun buildRoutePoints(arDispatches: List<Any>?): List<RoutePointModel> {
        val routePoints = mutableListOf<RoutePointModel>()
        val dispatchColorMap = mutableMapOf<String, Int>()
        var colorIndex = 0
        val arrColors = intArrayOf(
            context.getColor(R.color.routes_one),
            context.getColor(R.color.grey_divider),
            context.getColor(R.color.switch_thumb_on),
            context.getColor(R.color.routes_four)
        )
        arDispatches.orEmpty().forEach { dispatch ->
            val infoDispatchModel = dispatch as? dynamic ?: return@forEach
            val dispatchNumber = infoDispatchModel.getDispatchNumber() ?: "Default"
            val color = dispatchColorMap.getOrPut(dispatchNumber) {
                val assignedColor = arrColors[colorIndex % arrColors.size]
                colorIndex++
                assignedColor
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
            val destinyAddressList = infoDispatchModel.destinyAdress ?: emptyList<String>()
            val destinyCoordinatesList = infoDispatchModel.dropOffCoordinates ?: emptyList<String>()
            if (destinyAddressList.size == destinyCoordinatesList.size) {
                destinyAddressList.forEachIndexed { idx, address ->
                    routePoints.add(
                        RoutePointModel(
                            type = RoutePointModel.RoutePointType.DROP_OFF,
                            address = address,
                            coordinatesTag = destinyCoordinatesList[idx],
                            order = infoDispatchModel.orderDropOff,
                            dispatchNumber = infoDispatchModel.getDispatchNumber(),
                            titleColor = color
                        )
                    )
                }
            }
        }
        return routePoints
    }
    fun onRoutePointClick(routePoint: RoutePointModel) {
        openNavigatorApp(routePoint.coordinatesTag, routePoint.address)
    }
    fun onButtonClick(action: DestinationMapButtonAction) {
        when (action) {
            DestinationMapButtonAction.Back -> {
viewModelScope.launch { emitEffect(DestinationMapUiEffect.NavigateBack)
}
}
            DestinationMapButtonAction.ShowDialog -> {
viewModelScope.launch { emitEffect(
}
}
                DestinationMapUiEffect.ShowDialog(
                    DestinationMapDialogState(
                        title = "Dialog",
                        message = "Example dialog content"
                    )
                )
            )
            DestinationMapButtonAction.OpenNavigator -> openNavigatorApp(null, null)
        }
    }
    fun onDialogDismissed() {
        emitEffect(DestinationMapUiEffect.HideDialog)
    }
    fun onDialogPrimaryAction() {
        emitEffect(DestinationMapUiEffect.HideDialog)
    }
    fun onDialogSecondaryAction() {
        emitEffect(DestinationMapUiEffect.HideDialog)
    }
    fun openNavigatorApp(coordinates: String?, street: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "openNavigatorApp: Coordinates = $coordinates, Street = $street")
            val navigatorIntent = when {
                !coordinates.isNullOrEmpty() && !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName(coordinates, street)
                !coordinates.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorWithCoordinates(coordinates)
                !street.isNullOrEmpty() ->
                    navigatorUseCase.openNavigatorApp(street)
                else ->
                    navigatorUseCase.openNavigatorApp()
            }
            _uiEffect.emit(DestinationMapUiEffect.OpenNavigator(navigatorIntent))
        }
    }
    fun emitEffect(effect: DestinationMapUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
/** State **/
data class DestinationMapUiState(
    val routePoints: List<RoutePointModel> = emptyList(),
    val buttonsState: DestinationMapButtonsState = DestinationMapButtonsState()
)
/** UI Events from composable to VM **/
sealed interface DestinationMapButtonAction {
    data object Back : DestinationMapButtonAction
    data object ShowDialog : DestinationMapButtonAction
    data object OpenNavigator : DestinationMapButtonAction
}
/** UI Effects from VM to composable **/
sealed interface DestinationMapUiEffect {
    data class OpenNavigator(val intent: Intent?) : DestinationMapUiEffect
    data class ShowDialog(val dialogState: DestinationMapDialogState) : DestinationMapUiEffect
    data object HideDialog : DestinationMapUiEffect
    data object NavigateBack : DestinationMapUiEffect
}
data class DestinationMapDialogState(
    val title: String,
    val message: String
)
