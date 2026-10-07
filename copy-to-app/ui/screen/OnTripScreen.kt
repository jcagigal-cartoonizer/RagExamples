package ifac.td.taxi.ui.screen
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
import ifac.td.taxi.ui.screen.OnTripScreen
// # Block 422-4: import android.content.Intent
@Composable
fun OnTripScreen(
    navController: NavController,
    viewModel: OnTripComposeViewModel,
    locationAllowedByCentral: Boolean,
    shiftStatusCurrentStatus: Int?,
    shiftIsManual: Boolean,
    tripFromDispatch: Boolean?,
    dispatchId: Long?,
    dispatch: ifac.td.taxi.viewmodel.model.InfoDispatchModel?,
    roofLight: Boolean?,
    hiredZoneExists: Boolean,
    hasTaximeterConnection: Boolean,
    canDoManualTrips: Boolean,
    onShowHeader: (Boolean) -> Unit,
    onTopBarNextVisible: (Boolean) -> Unit,
    onTopBarNextClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<OnTripDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.onScreenStarted(locationAllowedByCentral)
    }
    LaunchedEffect(
        locationAllowedByCentral,
        hiredZoneExists,
        hasTaximeterConnection,
        shiftStatusCurrentStatus,
        shiftIsManual,
        tripFromDispatch,
        roofLight,
        canDoManualTrips,
        dispatch
    ) {
        viewModel.updateButtonsForTrip(
            locationAllowedByCentral = locationAllowedByCentral,
            hiredZoneExists = hiredZoneExists,
            hasTaximeterConnection = hasTaximeterConnection,
            currentStatus = shiftStatusCurrentStatus,
            isManual = shiftIsManual,
            tripFromDispatch = tripFromDispatch,
            dispatch = dispatch,
            roofLight = roofLight,
            canDoManualTrips = canDoManualTrips
        )
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                OnTripUiEffect.NavigateToEstimateFixedPrice -> navController.navigate(R.id.action_onTripFragment_to_FixedPriceMapFragment)
                OnTripUiEffect.NavigateToReceiptHistory -> navController.navigate(R.id.action_onTripFragment_to_receiptHistoryFragment)
                OnTripUiEffect.NavigateToMessage -> navController.navigate(R.id.action_onTripFragment_to_messageFragment)
                OnTripUiEffect.NavigateToContactCentral -> navController.navigate(R.id.action_onTripFragment_to_contactCentralFragment)
                OnTripUiEffect.NavigateToDispatchInfo -> navController.navigate(R.id.actionOnTripFragmentToInfoDispatchFragment)
                OnTripUiEffect.NavigateToDirections -> navController.navigate(R.id.actionOnTripFragmentToDirectionsFragment)
                OnTripUiEffect.NavigateToMacroZoning -> navController.navigate(R.id.action_onTripFragment_to_macroZoning)
                is OnTripUiEffect.NavigateToDeepLink -> navController.navigate(effect.deepLinkUri)
                is OnTripUiEffect.OpenExternalIntent -> {
                    if (effect.intentAction.isNotBlank()) {
                        context.startActivity(Intent(effect.intentAction))
                    }
                }
                OnTripUiEffect.ShowSelectNotificationDialog -> {
                    dialogState = OnTripDialogState(
                        title = context.getString(R.string.select_notification),
                        buttons = listOf(OnTripDialogButton.AT_DOOR, OnTripDialogButton.RIDER_IN_CAB)
                    )
                }
                OnTripUiEffect.ShowConfirmNoClientDialog -> {
                    dialogState = OnTripDialogState(
                        title = context.getString(R.string.confirm_no_client),
                        buttons = listOf(OnTripDialogButton.CANCEL, OnTripDialogButton.ACCEPT)
                    )
                }
                OnTripUiEffect.ShowConfirmHiredManualDialog -> {
                    dialogState = OnTripDialogState(
                        title = context.getString(R.string.dialog_hired_manual_title),
                        description = context.getString(R.string.confirm_change_to_hired),
                        buttons = listOf(OnTripDialogButton.CANCEL, OnTripDialogButton.ACCEPT)
                    )
                }
                OnTripUiEffect.ShowToastItopRestricted -> {
                    Toast.makeText(context, context.getString(R.string.itop_functionality_restricted), Toast.LENGTH_LONG).show()
                }
                OnTripUiEffect.ShowTopBarNext -> onTopBarNextVisible(true)
                OnTripUiEffect.HideTopBarNext -> onTopBarNextVisible(false)
                OnTripUiEffect.NoEffect -> Unit
            }
        }
    }
    dialogState?.let { dialog ->
        OnTripCustomDialog(
            state = dialog,
            onDismiss = { dialogState = null },
            onButtonClicked = { button ->
                dialogState = null
                when (button) {
                    OnTripDialogButton.AT_DOOR -> viewModel.sendAtTheDoorNotification(dispatch)
                    OnTripDialogButton.RIDER_IN_CAB -> viewModel.sendInCabNotification(dispatch)
                    OnTripDialogButton.ACCEPT -> {
                        if (shiftStatusCurrentStatus == com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_DISPATCHED) {
                            dispatch?.let { viewModel.goToHiredManual(it.id) }
                        } else {
                            viewModel.changeStateToPaymentManual()
                        }
                    }
                    OnTripDialogButton.CANCEL -> Unit
                }
            }
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        OnTripButtonGrid(
            buttons = uiState.buttons,
            locationAllowedByCentral = locationAllowedByCentral,
            hiredZoneExists = hiredZoneExists,
            shiftStatusCurrentStatus = shiftStatusCurrentStatus,
            dispatch = dispatch,
            roofLight = roofLight,
            onFixedPrice = { viewModel.onFixedPriceClicked() },
            onNotifications = {
                viewModel.onNotificationClicked(
                    hasAtDoor = dispatch?.isAtDoorNotificationEnabled() == true,
                    hasInCab = dispatch?.riderInCab == true
                )
            },
            onZoning = { viewModel.onZoningClicked(hiredZoneExists) },
            onNavigate = {
                if (shiftStatusCurrentStatus == com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_DISPATCHED) {
                    navController.navigate(R.id.actionOnTripFragmentToDirectionsFragment)
                } else {
                    viewModel.onNavigateClicked()
                }
            },
            onDispatchInfo = { viewModel.onDispatchInfoClicked() },
            onReceipts = { viewModel.onReceiptsClicked() },
            onMessages = { viewModel.onMessagesClicked() },
            onCentral = { viewModel.onCentralClicked() },
            onClient = { viewModel.onClientClicked(dispatchId) },
            onRoofLight = { viewModel.onRoofLightClicked(roofLight) }
        )
    }
}
