package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 453-7: import androidx.compose.foundation.layout.*
@Composable
fun TripRow(trip: Trip) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Trip #${trip.id}")
            Text(text = "Amount: ${trip.totalAmount}")
            Text(text = "Distance: ${trip.distance}")
        }
    }
}
