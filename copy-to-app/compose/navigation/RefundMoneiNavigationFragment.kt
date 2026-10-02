package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.RefundMoneiScreen
import ifac.td.taxi.compose.viewModel.RefundMoneiComposeViewModel
import ifac.td.taxi.viewModel.RefundMoneiViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 4-1: import android.os.Bundle
class RefundMoneiNavigationFragment : Fragment(R.layout.fragment_refund_monei_navigation) {
    private val viewModel: RefundMoneiViewModel by viewModels()
    private val args: RefundMoneiNavigationFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // If you have a header in the Activity, preserve your old behavior here
        // (replace with your actual activity API if needed)
        // (activity as? YourMainActivity)?.showHeader(false)
        viewModel.startRunnable(args.tripId)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            MaterialTheme {
                RefundMoneiRoute(
                    viewModel = viewModel,
                    onBack = {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // preserve your fragment back behavior if needed
            isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}
@androidx.compose.runtime.Composable
fun RefundMoneiRoute(
    viewModel: RefundMoneiViewModel,
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState // adapt if your VM exposes StateFlow/LiveData differently
    RefundMoneiScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                RefundMoneiUiEvent.OnCancelClicked -> onBack()
                RefundMoneiUiEvent.OnRefundClicked -> viewModel.onEvent(RefundMoneiUiEvent.OnRefundClicked)
                RefundMoneiUiEvent.OnConfirmRefund -> viewModel.onEvent(RefundMoneiUiEvent.OnConfirmRefund)
                RefundMoneiUiEvent.OnDismissDialog -> viewModel.onEvent(RefundMoneiUiEvent.OnDismissDialog)
            }
        }
    )
}
Create a simple layout with a `ComposeView`:
<?xml version="1.0" encoding="utf-8"?>
<androidx.compose.ui.platform.ComposeView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/compose_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <androidx.compose.ui.platform.ComposeView
        android:id="@+id/compose_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>
Replace your current `RefundMoneiFragment` destination with the new fragment:
<fragment
    android:id="@+id/refundMoneiFragment"
    android:name="ifac.td.taxi.ui.screen.RefundMoneiNavigationFragment"
    android:label="RefundMoneiFragment">
    <argument
        android:name="tripId"
        app:argType="long" />
</fragment>
In the example above, I used:
val uiState = viewModel.uiState
because your exact `RefundMoneiViewModel` API wasn’t included. If your state is exposed as a `StateFlow`, use `collectAsStateWithLifecycle()`.
Example:
@Composable
fun RefundMoneiRoute(
    viewModel: RefundMoneiViewModel,
    onBack: () -> Unit
) {
    val uiState = androidx.lifecycle.compose.collectAsStateWithLifecycle(
        viewModel.uiState
    ).value
    RefundMoneiScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                RefundMoneiUiEvent.OnCancelClicked -> onBack()
                else -> viewModel.onEvent(event)
            }
        }
    )
}
Your old Fragment called:
vModel.startRunnable(safeArgs.tripId)
Do the same in the new fragment, as shown above:
viewModel.startRunnable(args.tripId)
If `iMainActivity.navigateBack()` did something custom, replace the back action with that logic instead of `onBackPressedDispatcher`.
For example, if your activity has:
(requireActivity() as MainActivity).navigateBack()
use that inside `onBack`.
// # Block 157-2: import android.os.Bundle
class RefundMoneiNavigationFragment : Fragment(R.layout.fragment_refund_monei_navigation) {
    private val viewModel: RefundMoneiViewModel by viewModels()
    private val args: RefundMoneiNavigationFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.startRunnable(args.tripId)
        view.findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    RefundMoneiScreen(
                        state = viewModel.uiState, // adapt to your state source
                        onEvent = { event ->
                            when (event) {
                                RefundMoneiUiEvent.OnCancelClicked -> requireActivity().onBackPressedDispatcher.onBackPressed()
                                else -> viewModel.onEvent(event)
                            }
                        }
                    )
                }
            }
        }
    }
}
