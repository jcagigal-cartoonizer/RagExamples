package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ShiftsScreen
import ifac.td.taxi.compose.viewModel.ShiftsComposeViewModel
import ifac.td.taxi.viewModel.ShiftsViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class ShiftsNavigationFragment : Fragment() {
    private val viewModel: ShiftsComposeViewModel by viewModels()
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
                ShiftsScreen(
                    navController = findNavController(),
                    viewModel = viewModel
                )
            }
        }
    }
}
Replace the old fragment destination:
<fragment
    android:id="@+id/shiftsFragment"
    android:name="ifac.td.taxi.ui.screen.ShiftsFragment"
    android:label="fragment_shifts"
    tools:layout="@layout/fragment_shifts">
    <action
        android:id="@+id/action_shiftsFragment_to_shiftDetailFragment"
        app:destination="@id/shiftDetailFragment" />
</fragment>
with:
<fragment
    android:id="@+id/shiftsFragment"
    android:name="ifac.td.taxi.ui.screen.ShiftsNavigationFragment"
    android:label="fragment_shifts"
    tools:layout="@layout/fragment_shifts">
    <action
        android:id="@+id/action_shiftsFragment_to_shiftDetailFragment"
        app:destination="@id/shiftDetailFragment" />
</fragment>
In your composable you currently navigate like this:
val action =
    ifac.td.taxi.ui.screen.ShiftsFragmentDirections
        .actionShiftsFragmentToShiftDetailFragment(effect.shift)
navController.navigate(action)
That is fine **as long as the destination id remains `@id/shiftsFragment`** and the action remains declared under that destination in the graph.
So you do **not** need to change the action name or the Safe Args usage.
@Composable
fun ShiftsRoute(
    navController: NavController,
    viewModel: ShiftsComposeViewModel
) {
    ShiftsScreen(
        navController = navController,
        viewModel = viewModel
    )
}
Then in the fragment:
setContent {
    ShiftsRoute(
        navController = findNavController(),
        viewModel = viewModel
    )
}
