package ifac.td.taxi.ui.screen.components
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
// # Block 10-1: import android.content.Intent
@Composable
fun DestinationMapRoute(
    viewModel: DestinationMapComposeViewModel,
    onLaunchIntent: (Intent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isDialogVisible by remember { mutableStateOf(false) }
    var dialogContent by remember { mutableStateOf<DestinationMapDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is DestinationMapUiEffect.OpenNavigator -> {
                    effect.intent?.let(onLaunchIntent)
                }
                is DestinationMapUiEffect.ShowDialog -> {
                    dialogContent = effect.dialogState
                    isDialogVisible = true
                }
                DestinationMapUiEffect.HideDialog -> {
                    isDialogVisible = false
                    dialogContent = null
                }
                DestinationMapUiEffect.NavigateBack -> onBack()
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DestinationMapButtons(
                state = uiState.buttonsState,
                onClick = viewModel::onButtonClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.routePoints.isEmpty()) {
                Text(text = "No route points available")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.routePoints) { routePoint ->
                        RoutePointItem(
                            routePoint = routePoint,
                            onClick = { viewModel.onRoutePointClick(routePoint) }
                        )
                    }
                }
            }
        }
        if (isDialogVisible && dialogContent != null) {
            DestinationMapCustomDialogCustomDialog(
                state = dialogContent!!,
                onDismiss = {
                    isDialogVisible = false
                    viewModel.onDialogDismissed()
                },
                onPrimaryAction = {
                    viewModel.onDialogPrimaryAction()
                },
                onSecondaryAction = {
                    viewModel.onDialogSecondaryAction()
                }
            )
        }
    }
}
@Composable
fun RoutePointItem(
    routePoint: RoutePointModel,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = routePoint.address)
            Text(text = routePoint.coordinatesTag)
            Text(text = routePoint.dispatchNumber.orEmpty())
        }
    }
}
