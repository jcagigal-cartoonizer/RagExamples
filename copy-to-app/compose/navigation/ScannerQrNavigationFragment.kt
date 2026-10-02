package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ScannerQrScreen
import ifac.td.taxi.compose.viewModel.ScannerQrComposeViewModel
import ifac.td.taxi.viewModel.ScannerQrViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ScannerQrNavigationFragment : Fragment() {
    private val viewModel: ScannerQrViewModel by viewModels()
    private val args: ScannerQrNavigationFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    ScannerQrScreen(
                        uiState = uiState,
                        onEvent = { event ->
                            viewModel.onEvent(event)
                        },
                        uiEffects = viewModel.uiEffects,
                        onNavigateBack = {
                            findNavController().navigateUp()
                        },
                        onNavigateHome = {
                            findNavController().navigate(
                                ifac.td.taxi.ui.screen.ScannerQrNavigationFragmentDirections
                                    .actionScannerQrNavigationFragmentToHomeFragment()
                            )
                        },
                        onOpenScanner = { cameraPosition ->
                            // If your scanner is opened by an Activity/host callback,
                            // forward this event through the ViewModel or another shared component.
                            viewModel.onEvent(
                                ScannerQrUiEvent.OpenScannerRequested(cameraPosition)
                            )
                        },
                        onQrScanned = { result ->
                            viewModel.onEvent(ScannerQrUiEvent.QrScanned(result))
                        }
                    )
                }
            }
        }
    }
}
Replace the old fragment destination:
<fragment
    android:id="@+id/scannerQrFragment"
    android:name="ifac.td.taxi.ui.screen.ScannerQrFragment"
    android:label="ScannerQrFragment"
    tools:layout="@layout/fragment_scanner_qr">
with:
<fragment
    android:id="@+id/scannerQrFragment"
    android:name="ifac.td.taxi.ui.screen.ScannerQrNavigationFragment"
    android:label="ScannerQrNavigationFragment"
    tools:layout="@layout/fragment_scanner_qr">
    <argument
        android:name="serviceId"
        app:argType="string"
        app:nullable="true" />
</fragment>
Your old fragment interacted with:
In Compose, that logic is usually better moved into:
// # Block 101-2: import android.os.Bundle
class ScannerQrNavigationFragment : Fragment() {
    private val viewModel: ScannerQrViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ScannerQrScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    uiEffects = viewModel.uiEffects,
                    onNavigateBack = { findNavController().navigateUp() },
                    onNavigateHome = { findNavController().navigateUp() },
                    onOpenScanner = { },
                    onQrScanned = { }
                )
            }
        }
    }
}
