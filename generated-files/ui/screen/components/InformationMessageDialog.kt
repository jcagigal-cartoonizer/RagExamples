package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 268-4: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ifac.td.taxi.ui.screen.compose.InformationMessageInformationMessageDialogButtonType
import ifac.td.taxi.ui.screen.compose.state.InformationMessageButtonsState
@Composable
fun InformationMessageDialog(
    title: String,
    description: String,
    buttonsState: InformationMessageButtonsState.DialogButtonsState,
    onDismiss: () -> Unit,
    onButtonClick: (InformationMessageInformationMessageDialogButtonType) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        text = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (buttonsState.cancelVisible) {
                            Button(
                                onClick = { onButtonClick(InformationMessageInformationMessageDialogButtonType.CANCEL) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonsState.cancelContainerColor,
                                    contentColor = buttonsState.cancelContentColor
                                )
                            ) {
                                Text(text = "CANCEL")
                            }
                        }
                        if (buttonsState.acceptVisible) {
                            Button(
                                onClick = { onButtonClick(InformationMessageInformationMessageDialogButtonType.ACCEPT) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonsState.acceptContainerColor,
                                    contentColor = buttonsState.acceptContentColor
                                )
                            ) {
                                Text(text = "ACCEPT")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
These helpers let you keep a button style closer to the legacy custom view behavior.
