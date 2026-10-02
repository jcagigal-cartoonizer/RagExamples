package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.InfoDispatchScreen
import ifac.td.taxi.compose.viewModel.InfoDispatchComposeViewModel
import ifac.td.taxi.viewModel.InfoDispatchViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class InfoDispatchNavigationFragment : Fragment() {
    private val viewModel: InfoDispatchComposeViewModel by viewModels()
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
                    InfoDispatchScreen(
                        navController = findNavController(),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
Replace the old destination:
<fragment
    android:id="@+id/infoDispatchFragment"
    android:name="ifac.td.taxi.ui.screen.InfoDispatchFragment"
    android:label="InfoDispatchFragment"
    tools:layout="@layout/fragment_info_dispatch">
with:
<fragment
    android:id="@+id/infoDispatchFragment"
    android:name="ifac.td.taxi.ui.screen.InfoDispatchNavigationFragment"
    android:label="InfoDispatchNavigationFragment">
Your current composable already works as the UI layer:
@Composable
fun InfoDispatchScreen(
    navController: NavController,
    viewModel: InfoDispatchComposeViewModel
)
So the fragment only needs to host it.
MaterialTheme {
    InfoDispatchScreen(...)
}
TaxiTheme {
    InfoDispatchScreen(
        navController = findNavController(),
        viewModel = viewModel
    )
}
