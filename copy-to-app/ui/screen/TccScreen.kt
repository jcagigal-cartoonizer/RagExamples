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
// # Block 350-3: import androidx.compose.foundation.layout.*
@Composable
fun TccScreen(
    tripId: Long?,
    navController: NavController,
    viewModel: TccComposeViewModel,
    onToast: (Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(tripId) {
        viewModel.loadDispatch(tripId)
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is TccUiEffect.ToastRes -> onToast(effect.resId)
                is TccUiEffect.NavigateToSignature -> {
                    navController.navigate(
                        ifac.td.taxi.PaymentDirections.goToSignatureFragment(effect.dispatchId)
                    )
                }
                is TccUiEffect.NavigateToVoucher -> {
                    navController.navigate(
                        ifac.td.taxi.PaymentDirections.goToCropImageViewFragment(effect.dispatchId)
                    )
                }
                is TccUiEffect.NavigateToQr -> {
                    navController.navigate(
                        ifac.td.taxi.PaymentDirections.goToScannerQRFragment(effect.dispatchId)
                    )
                }
                is TccUiEffect.FinishTcc -> Unit
                is TccUiEffect.NavigateBack -> navController.popBackStack()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        } else {
            TccContent(
                dispatch = uiState.dispatch,
                buttonsState = uiState.buttonsState,
                onAccept = { att1, att2, att3, att4 ->
                    uiState.dispatch?.let { dispatch ->
                        viewModel.onAcceptClicked(
                            tripId = tripId,
                            dispatch = dispatch,
                            att1 = att1,
                            att2 = att2,
                            att3 = att3,
                            att4 = att4
                        )
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        when (val dialog = uiState.dialogState) {
            is TccDialogState.Hidden -> Unit
            is TccDialogState.Error -> {
                TccCustomDialogCustomDialog(
                    visible = true,
                    title = stringResource(R.string.dialog_error_title),
                    message = dialog.message,
                    confirmText = stringResource(android.R.string.ok),
                    onConfirm = viewModel::dismissDialog,
                    onDismiss = viewModel::dismissDialog
                )
            }
            is TccDialogState.ConfirmAccept -> {
                TccCustomDialogCustomDialog(
                    visible = true,
                    title = stringResource(R.string.dialog_error_title),
                    message = dialog.dispatchTitle ?: "",
                    confirmText = stringResource(android.R.string.ok),
                    onConfirm = viewModel::dismissDialog,
                    onDismiss = viewModel::dismissDialog
                )
            }
        }
    }
}
@Composable
fun TccContent(
    dispatch: Dispatch?,
    buttonsState: TccButtonsState,
    onAccept: (Int?, Int?, Int?, Int?) -> Unit,
    onCancel: () -> Unit
) {
    val zeroToNine = remember { (0..9).toList() }
    var att1 by remember { mutableIntStateOf(1) }
    var att2 by remember { mutableIntStateOf(1) }
    var att3 by remember { mutableIntStateOf(1) }
    var att4 by remember { mutableIntStateOf(1) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        dispatch?.let {
            TccAttributeRow(
                state = buttonsState.attributes1,
                value = att1,
                onValueChange = { att1 = it },
                items = zeroToNine
            )
            TccAttributeRow(
                state = buttonsState.attributes2,
                value = att2,
                onValueChange = { att2 = it },
                items = zeroToNine
            )
            TccAttributeRow(
                state = buttonsState.attributes3,
                value = att3,
                onValueChange = { att3 = it },
                items = zeroToNine
            )
            TccAttributeRow(
                state = buttonsState.attributes4,
                value = att4,
                onValueChange = { att4 = it },
                items = zeroToNine
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.cancel.visible) {
                TccCustomButton(
                    text = "Cancel",
                    modifier = Modifier.weight(1f),
                    style = TccButtonStyle.cancel(),
                    enabled = buttonsState.cancel.enabled,
                    loading = buttonsState.cancel.loading,
                    onClick = onCancel
                )
            }
            Spacer(Modifier.width(12.dp))
            if (buttonsState.accept.visible) {
                TccCustomButton(
                    text = "Accept",
                    modifier = Modifier.weight(1f),
                    style = TccButtonStyle.accept(),
                    enabled = buttonsState.accept.enabled,
                    loading = buttonsState.accept.loading,
                    onClick = { onAccept(att1, att2, att3, att4) }
                )
            }
        }
    }
}
@Composable
fun TccAttributeRow(
    state: AttributeSpinnerState,
    value: Int,
    onValueChange: (Int) -> Unit,
    items: List<Int>
) {
    if (!state.visible) return
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = state.title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        TccDropdown(
            value = value,
            items = items,
            onValueChange = onValueChange
        )
    }
}
@Composable
fun TccDropdown(
    value: Int,
    items: List<Int>,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = value.toString())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Immutable
data class TccButtonStyle(
    val background: Color,
    val content: Color,
    val disabledBackground: Color,
    val disabledContent: Color,
    val border: Color? = null
) {
    companion object {
        fun accept() = TccButtonStyle(
            background = Color(0xFF2E7D32),
            content = Color.White,
            disabledBackground = Color(0xFF9E9E9E),
            disabledContent = Color(0xFFE0E0E0)
        )
        fun cancel() = TccButtonStyle(
            background = Color(0xFFD32F2F),
            content = Color.White,
            disabledBackground = Color(0xFF9E9E9E),
            disabledContent = Color(0xFFE0E0E0)
        )
    }
}
@Composable
fun TccCustomButton(
    text: String,
    modifier: Modifier = Modifier,
    style: TccButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    val bg = if (enabled) style.background else style.disabledBackground
    val fg = if (enabled) style.content else style.disabledContent
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg,
            disabledContentColor = fg
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = fg
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text)
    }
}
fun Dispatch.toTccButtonsState(isLandscape: Boolean): TccButtonsState {
    fun attr(title: String?) = AttributeSpinnerState(
        visible = !title.isNullOrEmpty(),
        title = title.orEmpty(),
        value = 1
    )
    return TccButtonsState(
        accept = TccButtonState(visible = true, enabled = true, loading = false),
        cancel = TccButtonState(visible = true, enabled = true, loading = false),
        attributes1 = attr(attribute1Title),
        attributes2 = attr(attribute2Title),
        attributes3 = attr(attribute3Title),
        attributes4 = attr(attribute4Title),
    )
}
A helper:
@Composable
fun VisibilityWrapper(
    visible: Boolean,
    goneWhenHidden: Boolean,
    content: @Composable () -> Unit
) {
    if (visible) {
        content()
    } else if (!goneWhenHidden) {
        Box(modifier = Modifier.alpha(0f)) {
            content()
        }
    }
}
