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
// # Block 364-6: import androidx.compose.foundation.BorderStroke
@Composable
fun ScannerQrButton(
    spec: ScannerButtonSpec,
    onClick: () -> Unit
) {
    if (!spec.visible) return
    val colors = ButtonDefaults.buttonColors(
        containerColor = spec.backgroundColor,
        contentColor = spec.contentColor,
        disabledContainerColor = spec.backgroundColor.copy(alpha = 0.5f),
        disabledContentColor = spec.contentColor.copy(alpha = 0.6f)
    )
    val border = spec.borderColor?.let { BorderStroke(1.dp, it) }
    Button(
        onClick = onClick,
        enabled = spec.enabled && !spec.loading,
        colors = colors,
        border = border,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (spec.loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
                color = spec.contentColor
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(text = spec.text)
    }
}
Your Fragment previously used:
In Compose, you preserve that by injecting callbacks from your host Activity/Fragment:
ScannerQrScreen(
    uiState = state,
    onEvent = viewModel::onEvent,
    uiEffects = viewModel.uiEffects,
    onNavigateBack = { iMainActivity.navigateBack() },
    onNavigateHome = { iMainActivity.navigateTo(NavGraphDirections.goToHomeFragment()) },
    onOpenScanner = { cameraPosition ->
        iMainActivity.openScanner(
            object : OnScannerResultCallback {
                override fun onSuccess(result: String) {
                    viewModel.onScannerResult(
                        rawResult = result,
                        infoDispatchModel = sharedViewModel.dispatchFlow.value,
                        trip = sharedViewModel.tripFlow.value,
                        updatePrintFlowCallback = sharedViewModel.updatePrintFlowCallback
                    )
                }
            },
            cameraPosition
        )
    },
    onQrScanned = { raw ->
        viewModel.onEvent(ScannerQrUiEvent.ScannerQrResult(raw))
    }
)
That maps well to Compose and avoids the old fragment `eventCollector` pattern.
A Compose host that replaces the fragment:
@Composable
fun ScannerQrRoute(
    viewModel: ScannerQrComposeViewModel,
    sharedViewModel: MainActivityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    openScanner: (Int, (String) -> Unit) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    ScannerQrScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        uiEffects = viewModel.uiEffects,
        onNavigateBack = onNavigateBack,
        onNavigateHome = onNavigateHome,
        onOpenScanner = { cameraPosition ->
            openScanner(cameraPosition) { rawResult ->
                viewModel.onScannerResult(
                    rawResult = rawResult,
                    infoDispatchModel = sharedViewModel.dispatchFlow.value,
                    trip = sharedViewModel.tripFlow.value,
                    updatePrintFlowCallback = sharedViewModel.updatePrintFlowCallback
                )
            }
        },
        onQrScanned = { raw -> viewModel.onEvent(ScannerQrUiEvent.ScannerQrResult(raw)) }
    )
}
Use:
