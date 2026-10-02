package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.AddAmountScreen
import ifac.td.taxi.compose.viewModel.AddAmountComposeViewModel
import ifac.td.taxi.viewModel.AddAmountViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
