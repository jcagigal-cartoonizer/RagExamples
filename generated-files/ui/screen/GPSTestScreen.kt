package ifac.td.taxi.ui.screen
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
// # Block 275-3: import android.content.Intent
@Composable
fun GPSTestScreen(
    viewModel: ifac.td.taxi.viewmodel.GPSTestComposeViewModel,
    onNavigateBack: () -> Unit,
    launchIntent: (Intent) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    GPSTestUiEffect.NavigateBack -> onNavigateBack()
                    is GPSTestUiEffect.LaunchIntent -> launchIntent(effect.intent)
                    is GPSTestUiEffect.ShowDialog -> Unit
                    GPSTestUiEffect.DismissDialog -> Unit
                    is GPSTestUiEffect.Toast -> Unit
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = uiState.gpsType, style = MaterialTheme.typography.titleMedium)
            StatusRow("Hour", uiState.utcTime)
            StatusRow("Satellites", uiState.satellites)
            if (uiState.power.isNotEmpty()) StatusRow("Power", uiState.power)
            StatusRow("HDOP", uiState.hdop)
            StatusRow("Latitude", uiState.latitude)
            StatusRow("Longitude", uiState.longitude)
            if (uiState.isExternalGPS) {
                StatusRow("Speed", uiState.speed)
                StatusRow("Angle", uiState.heading)
            }
            Spacer(modifier = Modifier.height(16.dp))
            GPSTestButtons(
                buttons = uiState.buttons,
                onAcceptClick = viewModel::onAcceptClicked,
                onGpsClick = viewModel::onGpsClicked
            )
        }
        when (val dialog = uiState.dialog) {
            is GPSTestDialogState.ConfirmExit -> {
                GPSTestCustomDialogCustomDialog(
                    title = dialog.title,
                    message = dialog.message,
                    onConfirm = {
                        viewModel.onDialogDismiss()
                        onNavigateBack()
                    },
                    onDismiss = viewModel::onDialogDismiss
                )
            }
            GPSTestDialogState.Hidden -> Unit
        }
    }
}
@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value)
    }
}
Here’s a richer state holder for buttons, including XML-like styling support.
