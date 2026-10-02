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
