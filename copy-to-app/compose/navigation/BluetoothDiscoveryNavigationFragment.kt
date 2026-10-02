package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.BluetoothDiscoveryScreen
import ifac.td.taxi.compose.viewModel.BluetoothDiscoveryComposeViewModel
import ifac.td.taxi.viewModel.BluetoothDiscoveryViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 5-1: import android.content.Intent
class BluetoothDiscoveryNavigationFragment : Fragment() {
    private val viewModel: BluetoothDiscoveryViewModel by viewModels()
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
                    BluetoothDiscoveryNavigationRoute(
                        viewModel = viewModel,
                        navBack = { findNavController().navigateUp() },
                        onRequestBluetoothPermission = { permission ->
                            // If your app already handles permission requests via activity,
                            // call that API here. Otherwise, route to your permission screen.
                            // Example navigation can be done with Safe Args / directions if needed.
                        },
                        onRequestLocationPermission = { permission ->
                            // Same as above.
                        },
                        onStartActivity = { intent ->
                            startActivity(intent)
                        },
                        onConnectTaximeter = {
                            // call into activity or shared logic if needed
                        }
                    )
                }
            }
        }
    }
}
