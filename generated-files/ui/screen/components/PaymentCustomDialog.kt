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
import  androidx.compose.ui.window.Dialog
// # Block 562-8: import androidx.compose.foundation.clickable
@Composable
fun PaymentCustomDialog(
    dialog: PaymentDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (PaymentDialogButton, String?) -> Unit,
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var checked by remember { mutableStateOf(false) }
    var selectedOptionId by remember { mutableStateOf<Int?>(null) }
    AlertDialog(
        onDismissRequest = { if (dialog.isCancellable) onDismiss() },
        title = { Text(dialog.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = dialog.description,
                    textAlign = if (dialog.centerText) androidx.compose.ui.text.style.TextAlign.Center
                    else androidx.compose.ui.text.style.TextAlign.Start
                )
                if (dialog.listOptions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dialog.listOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOptionId = option.id },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOptionId == option.id,
                                    onClick = { selectedOptionId = option.id }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(option.title)
                            }
                        }
                    }
                }
                if (dialog.editTextHint != null) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (dialog.editTextMaxLength == null || it.text.length <= dialog.editTextMaxLength) text = it },
                        placeholder = { Text(dialog.editTextHint) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (dialog.showCheckBox && dialog.checkBoxText != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = checked, onCheckedChange = { checked = it })
                        Spacer(Modifier.width(8.dp))
                        Text(dialog.checkBoxText)
                    }
                }
            }
        },
        confirmButton = {
            val hasAccept = dialog.buttons.contains(PaymentDialogButton.ACCEPT)
            if (hasAccept) {
                TextButton(onClick = {
                    val payload = if (dialog.editTextHint != null) text.text else null
                    onButtonClick(PaymentDialogButton.ACCEPT, payload)
                }) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            if (dialog.buttons.contains(PaymentDialogButton.CANCEL)) {
                TextButton(onClick = { onButtonClick(PaymentDialogButton.CANCEL, null) }) {
                    Text("Cancel")
                }
            }
            if (dialog.buttons.contains(PaymentDialogButton.RETRY)) {
                TextButton(onClick = { onButtonClick(PaymentDialogButton.RETRY, null) }) {
                    Text("Retry")
                }
            }
            if (dialog.buttons.contains(PaymentDialogButton.OTHERS)) {
                TextButton(onClick = { onButtonClick(PaymentDialogButton.OTHERS, null) }) {
                    Text("Others")
                }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
Example:
LaunchedEffect(Unit) {
    viewModel.uiEffect.collectLatest { effect ->
        when (effect) {
            is PaymentUiEffect.NavigateTo -> navController.navigate(effect.routeId)
            is PaymentUiEffect.ShowToast -> showToast(effect.messageRes)
            else -> Unit
        }
    }
}
To fully replicate the fragment you’ll want to move these into state/effects gradually:
