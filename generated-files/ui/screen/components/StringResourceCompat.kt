package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.ui.screen.state.AddAmountButtonsState
object AddAmountButtonStyles {
    @Composable
    fun acceptColors(state: AddAmountButtonsState): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = state.acceptContainerColor,
            contentColor = state.acceptContentColor,
            disabledContainerColor = state.acceptDisabledContainerColor,
            disabledContentColor = state.acceptDisabledContentColor
        )
    }
    @Composable
    fun cancelColors(state: AddAmountButtonsState): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = state.cancelContainerColor,
            contentColor = state.cancelContentColor,
            disabledContainerColor = state.cancelDisabledContainerColor,
            disabledContentColor = state.cancelDisabledContentColor
        )
    }
    @Composable
    fun outlineBorder(): BorderStroke =
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
}
@Composable
fun AddAmountButtons(
    state: AddAmountButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancelVisible) {
            Button(
                onClick = onCancel,
                enabled = state.cancelEnabled,
                colors = AddAmountButtonStyles.cancelColors(state),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text("Cancel")
            }
        }
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                colors = AddAmountButtonStyles.acceptColors(state),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text("Accept")
            }
        }
    }
}
