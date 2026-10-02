package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.DashboardScreen
import ifac.td.taxi.compose.viewModel.DashboardComposeViewModel
import ifac.td.taxi.viewModel.DashboardViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class DashboardNavigationFragment : androidx.fragment.app.Fragment() {
    private val viewModel: DashboardViewModel by viewModels()
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
                val uiState = viewModel.uiState
                LaunchedEffect(Unit) {
                    viewModel.loadDataIfNeeded()
                }
                DashboardScreen(
                    uiState = uiState,
                    onEvent = { event ->
                        when (event) {
                            is DashboardUiEvent.OnHeaderClicked -> {
                                viewModel.onHeaderClicked(event.action)
                            }
                            is DashboardUiEvent.ZoneClicked -> {
                                viewModel.onZoneClicked(event.zone)
                            }
                            is DashboardUiEvent.ZoneLongClicked -> {
                                viewModel.onZoneLongClicked(event.zone)
                            }
                            DashboardUiEvent.DismissDialog -> {
                                viewModel.dismissDialog()
                            }
                            DashboardUiEvent.ConfirmDialog -> {
                                viewModel.confirmDialog()
                            }
                            DashboardUiEvent.CancelDialog -> {
                                viewModel.cancelDialog()
                            }
                        }
                    },
                    onNavigateBack = {
                        findNavController().popBackStack()
                    },
                    onOpenZoneDetails = { zone: ZoneModel ->
                        // Navigate to your zone details destination here
                        // Example:
                        // findNavController().navigate(
                        //     R.id.action_dashboardFragment_to_zoneDetailsFragment
                        // )
                    }
                )
            }
        }
    }
}
uiState: DashboardUiState
your `DashboardViewModel` should expose it as Compose state or observable state that Compose can read.
A simple example:
class DashboardViewModel : androidx.lifecycle.ViewModel() {
    val uiState: DashboardUiState = DashboardUiState()
    fun loadDataIfNeeded() {
        // load dashboard data
    }
    fun onHeaderClicked(action: DashboardHeaderAction) {
        // handle action
    }
    fun onZoneClicked(zone: ZoneModel) {
        // handle click
    }
    fun onZoneLongClicked(zone: ZoneModel) {
        // handle long click
    }
    fun dismissDialog() {}
    fun confirmDialog() {}
    fun cancelDialog() {}
}
