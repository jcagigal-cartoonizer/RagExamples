package ifac.td.taxi.ui.screen
import ifac.td.taxi.ui.screen.components.AboutScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 70-2: import androidx.compose.foundation.Image
@Composable
fun AboutScreen(
    uiState: AboutUiState,
    onPrivacyClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onLogoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonsState = uiState.buttonsState
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = uiState.appInfo,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.privacyPolicyText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier.clickable(enabled = true) {
                    onPrivacyClick()
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.showBluetoothInfo && uiState.bluetoothInfoText.isNotBlank()) {
                Text(
                    text = uiState.bluetoothInfoText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            AboutButton(
                text = buttonsState.accept.label,
                enabled = buttonsState.accept.enabled,
                visible = buttonsState.accept.visible,
                colors = buttonsState.accept.colors,
                onClick = onAcceptClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            // logo tap area (can be Image in real UI)
            Text(
                text = uiState.logoText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onLogoClick() }
                    .padding(8.dp)
            )
        }
    }
}
