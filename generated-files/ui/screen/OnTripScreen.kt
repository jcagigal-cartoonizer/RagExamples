package ifac.td.taxi.ui.screen
import ifac.td.taxi.compose.viewmodel.OnTripComposeViewModel
import ifac.td.taxi.ui.screen.components.OnTripUiEffect
import ifac.td.taxi.ui.screen.components.OnTripScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
