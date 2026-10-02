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
class AddAmountNavigationFragment : androidx.fragment.app.Fragment() {
    private val viewModel: AddAmountComposeViewModel by viewModels()
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
                    AddAmountNavigationRoute(
                        viewModel = viewModel,
                        onBack = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}
@Composable
fun AddAmountNavigationRoute(
    viewModel: AddAmountComposeViewModel,
    onBack: () -> Unit
) {
    AddAmountScreen(
        viewModel = viewModel,
        onBack = onBack
    )
}
Replace your current fragment destination:
<fragment
    android:id="@+id/addAmountFragment"
    android:name="ifac.td.taxi.ui.screen.AddAmountFragment"
    android:label="fragment_add_amount"
    tools:layout="@layout/fragment_add_amount">
    <argument
        android:name="tripId"
        app:argType="long" />
</fragment>
with:
<fragment
    android:id="@+id/addAmountFragment"
    android:name="ifac.td.taxi.ui.screen.AddAmountNavigationFragment"
    android:label="fragment_add_amount">
    <argument
        android:name="tripId"
        app:argType="long" />
</fragment>
Your current composable `AddAmountScreen(viewModel, onBack, onNavigateBack)` already handles the UI, but the fragment above only passes `onBack`.
then your `AddAmountComposeViewModel` must expose the same logic that the old `AddAmountViewModel` did, and the fragment should pass the trip argument into the ViewModel if needed.
For example, if your composable ViewModel has a function like:
fun loadTrip(tripId: Long)
then you can call it in `onViewCreated()` or after reading `arguments`.
Here is a slightly more complete version:
// # Block 88-2: import android.os.Bundle
class AddAmountNavigationFragment : Fragment() {
    private val viewModel: AddAmountComposeViewModel by viewModels()
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val tripId = arguments?.getLong("tripId") ?: 0L
        viewModel.onTripIdReceived(tripId) // implement in ViewModel if needed
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    AddAmountScreen(
                        viewModel = viewModel,
                        onBack = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}
