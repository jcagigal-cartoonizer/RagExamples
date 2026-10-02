package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.ContactCentralScreen
import ifac.td.taxi.compose.viewModel.ContactCentralComposeViewModel
import ifac.td.taxi.viewModel.ContactCentralViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
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
