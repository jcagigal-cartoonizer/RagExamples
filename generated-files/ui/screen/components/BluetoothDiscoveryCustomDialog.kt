package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 769-6: import androidx.compose.foundation.background
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
@Composable
fun BluetoothDiscoveryCustomDialog(
    state: BluetoothDiscoveryDialogState,
    onDismiss: () -> Unit,
    onAction: (BluetoothDiscoveryDialogAction) -> Unit
) {
    Dialog(onDismissRequest = { if (state.isCancelable) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 6.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    state.buttons.forEach { button ->
                        when (button.type) {
                            BluetoothDiscoveryDialogButtonType.Cancel -> {
                                OutlinedButton(
                                    onClick = { onAction(BluetoothDiscoveryDialogAction.Cancel) },
                                    modifier = Modifier.weight(1f)
                                ) { Text(button.label) }
                            }
                            BluetoothDiscoveryDialogButtonType.Accept -> {
                                Button(
                                    onClick = { onAction(BluetoothDiscoveryDialogAction.Accept) },
                                    modifier = Modifier.weight(1f)
                                ) { Text(button.label) }
                            }
                        }
                    }
                }
            }
        }
    }
}
To preserve your current navigation model:
In Compose, I modeled that as a callback:
onConnectTaximeter: () -> Unit
Then the screen performs:
currentOnConnectTaximeter()
navController.popBackStack()
A few parts of the legacy fragment depend on adapter-specific behavior:
In Compose, those are replaced by state:
Because your old code depends on app-specific components, you’ll probably still need to connect:
@Composable
fun BluetoothDiscoveryRoute(
    navController: NavController,
    viewModel: BluetoothDiscoveryComposeViewModel,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onConnectTaximeter: () -> Unit,
    onRequestPermission: (String) -> Unit
) {
    BluetoothDiscoveryScreen(
        viewModel = viewModel,
        navController = navController,
        onShowHeader = onShowHeader,
        onShowToast = onShowToast,
        onConnectTaximeter = onConnectTaximeter,
        onRequestPermission = onRequestPermission
    )
}
1. a **fully themed Material 2 / Material 3 version** matching your XML colors more closely,  
2. a **Hilt/Koin Compose ViewModel factory setup**, or  
3. a **direct translation of your adapter selection behavior into a Compose `LazyColumn` item model**.
