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
class HomeNavigationFragment : Fragment() {
    private val viewModel: HomeComposeViewModel by viewModels()
    private val sharedVm: MainActivityComposeStateHolder by viewModels(
        ownerProducer = { requireActivity() }
    )
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
                    HomeScreenWrapper(
                        navController = findNavController(),
                        viewModel = viewModel,
                        sharedVm = sharedVm
                    )
                }
            }
        }
    }
}
@Composable
fun HomeScreenWrapper(
    navController: NavController,
    viewModel: HomeComposeViewModel,
    sharedVm: MainActivityComposeStateHolder
) {
    HomeScreen(
        navController = navController,
        viewModel = viewModel,
        sharedVm = sharedVm,
        onShowToast = { /* connect to Activity/Fragment toast if needed */ },
        onBeep = { /* connect to beep if needed */ },
        onKeepScreenOn = { /* connect to activity window keep screen on if needed */ }
    )
}
Your composable currently requires:
onShowToast: (Int) -> Unit,
onBeep: (Int) -> Unit,
onKeepScreenOn: (Boolean) -> Unit
So in the fragment wrapper you must connect those to whatever your activity currently does.
val activity = requireActivity() as MainActivity
and then call the activity methods.
// # Block 82-2: import android.os.Bundle
class HomeNavigationFragment : Fragment() {
    private val viewModel: HomeComposeViewModel by viewModels()
    private val sharedVm: MainActivityComposeStateHolder by viewModels(
        ownerProducer = { requireActivity() }
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activity = requireActivity()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    HomeScreen(
                        navController = findNavController(),
                        viewModel = viewModel,
                        sharedVm = sharedVm,
                        onShowToast = { resId ->
                            if (activity is ToastProvider) activity.showToast(resId)
                        },
                        onBeep = { tone ->
                            if (activity is BeepProvider) activity.beep(tone)
                        },
                        onKeepScreenOn = { keepOn ->
                            if (activity is KeepScreenOnProvider) activity.keepScreenOn(keepOn)
                        }
                    )
                }
            }
        }
    }
}
interface ToastProvider {
    fun showToast(resId: Int)
}
interface BeepProvider {
    fun beep(tone: Int)
}
interface KeepScreenOnProvider {
    fun keepScreenOn(keep: Boolean)
}
