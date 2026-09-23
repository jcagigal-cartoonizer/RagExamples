package ifac.td.taxi.ui.screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 273-6: import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ChangeUserPasswordScreen(
    state: ChangeUserPasswordUiState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDialogAccepted: () -> Unit,
    dialogState: ChangeUserPasswordDialogState?,
    onDismissDialog: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = onCurrentPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current password") }
            )
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = onNewPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New password") }
            )
            OutlinedTextField(
                value = state.repeatPassword,
                onValueChange = onRepeatPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Repeat password") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ChangeUserPasswordButtons(
                state = state.buttonsState,
                onAcceptClick = onAcceptClick,
                onCancelClick = onCancelClick
            )
        }
        dialogState?.let { dialog ->
            ChangeUserPasswordDialog(
                state = dialog,
                onAccept = {
                    onDialogAccepted()
                    onDismissDialog()
                },
                onDismiss = onDismissDialog
            )
        }
    }
}
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun ChangeUserPasswordButtons(
    state: ChangeUserPasswordButtonsState,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.showCancel) {
            CustomLikeButton(
                modifier = Modifier.weight(1f),
                text = "Cancel",
                appearance = state.cancel,
                enabled = state.cancelEnabled,
                onClick = onCancelClick
            )
        }
        if (state.showAccept) {
            CustomLikeButton(
                modifier = Modifier.weight(1f),
                text = "Accept",
                appearance = state.accept,
                enabled = state.acceptEnabled,
                onClick = onAcceptClick
            )
        }
    }
}
@Composable
fun CustomLikeButton(
    modifier: Modifier,
    text: String,
    appearance: ButtonAppearance,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = Color(appearance.backgroundColor)
    val content = Color(appearance.contentColor)
    val stroke = appearance.strokeColor?.let { Color(it) }
    if (stroke != null && appearance.strokeWidthDp > 0) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(appearance.minHeightDp.dp),
            shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
            border = BorderStroke(appearance.strokeWidthDp.dp, stroke),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = bg,
                contentColor = content,
                disabledContainerColor = bg.copy(alpha = 0.5f),
                disabledContentColor = content.copy(alpha = 0.5f)
            )
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(appearance.minHeightDp.dp),
            shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = content,
                disabledContainerColor = bg.copy(alpha = 0.5f),
                disabledContentColor = content.copy(alpha = 0.5f)
            )
        ) {
            Text(text)
        }
    }
}
