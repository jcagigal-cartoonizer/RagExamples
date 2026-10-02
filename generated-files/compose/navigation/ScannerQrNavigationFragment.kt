package ifac.td.taxi.compose.navigation
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
