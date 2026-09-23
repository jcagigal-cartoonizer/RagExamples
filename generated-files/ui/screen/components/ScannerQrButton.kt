package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ScannerQrUiEvent
import ifac.td.taxi.ui.screen.components.ScannerQrButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
Because the original XML layouts were not included, I approximated the following based on common behavior in your existing code:
Then I can translate the exact padding, corner radius, typography, and colors 1:1 into Compose.
Use:
