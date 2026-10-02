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
// # Block 6-1: import androidx.compose.foundation.background
@Composable
fun AddAmountScreen(
    viewModel: AddAmountComposeViewModel,
    onBack: () -> Unit,
    onNavigateBack: () -> Unit = onBack,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<AddAmountComposeFragmentCustomDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                AddAmountUiEffect.NavigateBack -> onNavigateBack()
                is AddAmountUiEffect.ShowWarningDialog -> {
                    dialogState = AddAmountComposeFragmentCustomDialogState(
                        title = effect.title,
                        description = effect.description,
                        buttons = listOf(ButtonType.ACCEPT),
                        onAccept = {
                            dialogState = null
                        }
                    )
                }
            }
        }
    }
    LaunchedEffect(state.trip) {
        state.trip?.let { trip ->
            viewModel.onEvent(AddAmountUiEvent.OnTripLoaded(trip))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Amount",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            AmountField(
                label = "Service",
                value = state.serviceAmountText,
                hint = state.serviceAmountHint,
                enabled = state.buttonsState.canEditServiceAmount,
                onValueChange = { viewModel.onEvent(AddAmountUiEvent.OnServiceAmountChanged(it)) },
                onDone = { viewModel.onEvent(AddAmountUiEvent.OnFieldDone) }
            )
            if (state.buttonsState.showExtras) {
                AmountField(
                    label = "Extras",
                    value = state.extraAmountText,
                    hint = state.extraAmountHint,
                    enabled = false,
                    onValueChange = {},
                    onDone = {}
                )
            }
            if (state.buttonsState.showTolls) {
                AmountField(
                    label = "Tolls",
                    value = state.tollAmountText,
                    hint = state.tollAmountHint,
                    enabled = true,
                    onValueChange = { viewModel.onEvent(AddAmountUiEvent.OnTollAmountChanged(it)) },
                    onDone = { viewModel.onEvent(AddAmountUiEvent.OnFieldDone) }
                )
            }
            if (state.buttonsState.showTips) {
                AmountField(
                    label = "Tips",
                    value = state.tipAmountText,
                    hint = state.tipAmountHint,
                    enabled = true,
                    onValueChange = { viewModel.onEvent(AddAmountUiEvent.OnTipAmountChanged(it)) },
                    onDone = { viewModel.onEvent(AddAmountUiEvent.OnFieldDone) }
                )
            }
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text(state.totalAmountText, style = MaterialTheme.typography.titleLarge)
                }
            }
            AddAmountButtons(
                state = state.buttonsState,
                onAccept = { viewModel.onEvent(AddAmountUiEvent.OnAcceptClicked) },
                onCancel = { viewModel.onEvent(AddAmountUiEvent.OnCancelClicked) }
            )
        }
        dialogState?.let { ds ->
            AddAmountComposeFragmentAddAmountCustomDialog(
                state = ds,
                onDismiss = { dialogState = null }
            )
        }
    }
}
@Composable
fun AmountField(
    label: String,
    value: String,
    hint: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = { Text(hint) },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default,
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = { onDone() }
            )
        )
    }
}
