package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 273-3: import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.Color
data class DispatchReceivedButtonsState(
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptContainerColor: Color = Color(0xFF2E7D32),
    val acceptContentColor: Color = Color.White,
    val cancelContainerColor: Color = Color(0xFFD32F2F),
    val cancelContentColor: Color = Color.White,
    val useCancelConfirmationDialog: Boolean = false,
) {
    companion object {
        fun from(
            showRejectConfirmDialog: Boolean,
            hasDispatch: Boolean
        ): DispatchReceivedButtonsState {
            return DispatchReceivedButtonsState(
                acceptVisible = hasDispatch,
                cancelVisible = hasDispatch,
                acceptEnabled = hasDispatch,
                cancelEnabled = hasDispatch,
                useCancelConfirmationDialog = showRejectConfirmDialog,
            )
        }
    }
}
object DispatchReceivedButtonStyles {
    val AcceptContainer = Color(0xFF2E7D32)
    val AcceptContent = Color.White
    val CancelContainer = Color(0xFFD32F2F)
    val CancelContent = Color.White
    val DisabledContainer = Color(0xFFBDBDBD)
    val DisabledContent = Color(0xFFEEEEEE)
}
// // # Block 314-4: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.ui.screen.compose.state.DispatchReceivedButtonStyles
import ifac.td.taxi.ui.screen.compose.state.DispatchReceivedButtonsState
@Composable
fun DispatchReceivedButtons(
    state: DispatchReceivedButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onRequestRejectConfirmation: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.acceptEnabled)
                        state.acceptContainerColor else DispatchReceivedButtonStyles.DisabledContainer,
                    contentColor = if (state.acceptEnabled)
                        state.acceptContentColor else DispatchReceivedButtonStyles.DisabledContent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Aceptar")
            }
        }
        if (state.cancelVisible) {
            Button(
                onClick = {
                    if (state.useCancelConfirmationDialog) onRequestRejectConfirmation() else onCancel()
                },
                enabled = state.cancelEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.cancelEnabled)
                        state.cancelContainerColor else DispatchReceivedButtonStyles.DisabledContainer,
                    contentColor = if (state.cancelEnabled)
                        state.cancelContentColor else DispatchReceivedButtonStyles.DisabledContent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cancelar")
            }
        }
    }
}
