package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.HomeScreen
import ifac.td.taxi.compose.viewModel.HomeComposeViewModel
import ifac.td.taxi.viewModel.HomeViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
