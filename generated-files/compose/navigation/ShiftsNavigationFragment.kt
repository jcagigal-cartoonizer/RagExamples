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
