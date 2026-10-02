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
// # Block 6-1: import android.os.Bundle
class ContactCentralNavigationFragment : Fragment() {
    private val viewModel: ContactCentralComposeViewModel by viewModels()
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
                val navController = findNavController()
                ContactCentralScreen(
                    navController = navController,
                    viewModel = viewModel,
                    onShortBreakPressed = {
                        // If you need to call activity / shared logic, do it here.
                        // Example:
                        // (activity as? YourMainActivity)?.shortBreakButtonPressed()
                    }
                )
            }
        }
    }
}
Replace this destination:
<fragment
    android:id="@+id/contactCentralFragment"
    android:name="ifac.td.taxi.ui.screen.ContactCentralFragment"
    android:label="ContactCentralFragment">
    <action
        android:id="@+id/action_contactCentralFragment_to_predefinedMessageFragment"
        app:destination="@id/predefinedMessageFragment" />
    <action
        android:id="@+id/action_contactCentralFragment_to_informationMessageFragment"
        app:destination="@id/informationMessageFragment" />
</fragment>
with:
<fragment
    android:id="@+id/contactCentralFragment"
    android:name="ifac.td.taxi.ui.screen.ContactCentralNavigationFragment"
    android:label="ContactCentralFragment">
    <action
        android:id="@+id/action_contactCentralFragment_to_predefinedMessageFragment"
        app:destination="@id/predefinedMessageFragment" />
    <action
        android:id="@+id/action_contactCentralFragment_to_informationMessageFragment"
        app:destination="@id/informationMessageFragment" />
</fragment>
Your composable currently does this internally:
So this fragment wrapper works well **as long as** the `viewModel` passed to `ContactCentralScreen` is the same Compose ViewModel you already use there.
For example, if short break comes from an activity-scoped ViewModel, you can do:
private val sharedViewModel: MainActivityViewModel by viewModels({ requireActivity() })
or if you really need the activity instance logic, call it from the `onShortBreakPressed` callback.
// # Block 95-2: import android.os.Bundle
class ContactCentralNavigationFragment : Fragment() {
    private val viewModel: ContactCentralComposeViewModel by viewModels()
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
                ContactCentralScreen(
                    navController = findNavController(),
                    viewModel = viewModel,
                    onShortBreakPressed = {
                        // handle short break action here if needed
                    }
                )
            }
        }
    }
}
