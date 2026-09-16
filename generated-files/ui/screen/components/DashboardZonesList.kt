package ifac.td.taxi.ui.screen.state
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ### Zone lists

@Composable
private fun DashboardZoneLists(
    nearbyZones: List<ZoneModel>,
    farZones: List<ZoneModel>,
    actualZones: List<ZoneModel>,
    onZoneClick: (ZoneModel) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Text("Nearby zones", modifier = Modifier.padding(16.dp)) }
        items(nearbyZones) { zone ->
            TextButton(onClick = { onZoneClick(zone) }) {
                Text(zone.zone.name ?: "Zone")
            }
        }

        item { Text("Far zones", modifier = Modifier.padding(16.dp)) }
        items(farZones) { zone ->
            TextButton(onClick = { onZoneClick(zone) }) {
                Text(zone.zone.name ?: "Zone")
            }
        }

        item { Text("Actual zones", modifier = Modifier.padding(16.dp)) }
        items(actualZones) { zone ->
            Text(zone.zone.name ?: "Zone", modifier = Modifier.padding(16.dp))
        }
    }
}


