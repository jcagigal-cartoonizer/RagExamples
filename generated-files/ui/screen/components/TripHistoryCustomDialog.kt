package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TripHistoryCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 521-6: import androidx.compose.foundation.background
@Composable
fun TripHistoryCustomDialog(
    state: TripHistoryDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = state.title)
                Spacer(Modifier.padding(top = 12.dp))
                Text(text = state.description)
                Spacer(Modifier.padding(top = 20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF757575),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel")
                    }
                    if (state.showAccept) {
                        Spacer(Modifier.padding(start = 8.dp))
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}
navController.navigate(
    TripHistoryFragmentDirections
        .actionTripHistoryFragmentToReceiptHistoryFragment(-1)
)
and
navController.navigate(
    TripHistoryFragmentDirections
        .actionTripHistoryFragmentToReceiptHistoryFragment(tripId)
)
That is the closest equivalent to the fragment version.
In a pure Compose screen hosted in a `NavHost`, you would usually use route strings, but safe-args works if you keep this screen inside a Fragment container or use a navigation bridge.
Because the XML resources for:
were not included, I modeled the state holders and styling architecture so you can plug in the precise resource values.
1. a `TripHistoryButtonsState` with your exact resource colors  
2. a `CustomButton` Compose implementation that matches corner radius, stroke, elevation, and ripple  
3. a `TripHistoryCustomDialog` Compose implementation matching your XML layout pixel-for-pixel if you paste those XML files
composable("trip_history") {
    val vm: TripHistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    TripHistoryRoute(
        navController = navController,
        viewModel = vm
    )
}
