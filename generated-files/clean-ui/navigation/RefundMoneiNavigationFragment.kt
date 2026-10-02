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
