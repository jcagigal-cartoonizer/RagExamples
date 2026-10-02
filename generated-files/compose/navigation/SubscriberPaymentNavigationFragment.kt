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
class SubscriberPaymentNavigationFragment : Fragment() {
    private val viewModel: SubscriberPaymentComposeViewModel by viewModels()
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
                SubscriberPaymentScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onOpenScanner = { cameraPosition, onResult ->
                        // Hook this to your existing scanner flow if needed.
                        // Since you asked for Android + Compose only, this is left as a bridge.
                        // Example:
                        // (activity as? MainActivity)?.openScanner(cameraPosition, onResult)
                    },
                    onShowToast = { messageRes ->
                        // Optional bridge to host activity / snackbar / toast
                        // e.g. Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
Replace the old fragment destination:
<fragment
    android:id="@+id/subscriberPaymentFragment"
    android:name="ifac.td.taxi.ui.screen.SubscriberPaymentFragment"
    android:label="SubscriberPaymentFragment"
    tools:layout="@layout/fragment_subscriber_payment">
with:
<fragment
    android:id="@+id/subscriberPaymentFragment"
    android:name="ifac.td.taxi.ui.screen.SubscriberPaymentNavigationFragment"
    android:label="SubscriberPaymentNavigationFragment">
    <argument
        android:name="tripId"
        app:argType="long" />
    <argument
        android:name="user"
        app:argType="string"
        app:nullable="true" />
    <argument
        android:name="subscriber"
        app:argType="string"
        app:nullable="true" />
    <argument
        android:name="isFromIngenico"
        app:argType="boolean" />
</fragment>
Your old Fragment used `navArgs()`:
private val args: SubscriberPaymentFragmentArgs by navArgs()
Example:
val args = SubscriberPaymentNavigationFragmentArgs.fromBundle(requireArguments())
Then call something like:
viewModel.init(
    tripId = args.tripId,
    user = args.user,
    subscriber = args.subscriber,
    isFromIngenico = args.isFromIngenico
)
If the ViewModel needs the nav args at construction time, create it with a factory and keep using `by viewModels { ... }`.
onOpenScanner: (cameraPosition: Int, onResult: (String) -> Unit) -> Unit
you can connect that to your existing Activity scanner API from the Fragment. For example:
onOpenScanner = { cameraPosition, onResult ->
    (activity as? MainActivity)?.openScanner(
        callback = object : OnScannerResultCallback {
            override fun onSuccess(result: String) {
                onResult(result)
            }
        },
        cameraPosition = cameraPosition
    )
}
// # Block 123-2: import android.os.Bundle
class SubscriberPaymentNavigationFragment : Fragment() {
    private val viewModel: SubscriberPaymentComposeViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // If needed:
        // val args = SubscriberPaymentNavigationFragmentArgs.fromBundle(requireArguments())
        // viewModel.init(args.tripId, args.user, args.subscriber, args.isFromIngenico)
    }
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                SubscriberPaymentScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onOpenScanner = { _, _ -> },
                    onShowToast = { }
                )
            }
        }
    }
}
