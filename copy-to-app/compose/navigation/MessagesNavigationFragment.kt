package ifac.td.taxi.compose.navigation
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ifac.td.taxi.ui.screen.MessagesScreen
import ifac.td.taxi.compose.viewModel.MessagesComposeViewModel
import ifac.td.taxi.viewModel.MessagesViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
// # Block 6-1: import android.os.Bundle
class MessagesNavigationFragment : Fragment() {
    private val viewModel: MessagesComposeViewModel by viewModels()
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
                MessagesScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        findNavController().navigateUp()
                    },
                    onNavigateToMessageDetail = { messageId, skipAutoClose ->
                        val action =
                            MessagesNavigationFragmentDirections
                                .actionMessagesNavigationFragmentToMessageDetailFragment(
                                    messageId,
                                    skipAutoClose
                                )
                        findNavController().navigate(action)
                    }
                )
            }
        }
    }
}
Add this fragment to your nav graph where `MessageFragment` was used, or replace the destination with this new fragment.
<fragment
    android:id="@+id/messagesNavigationFragment"
    android:name="ifac.td.taxi.ui.screen.MessagesNavigationFragment"
    android:label="MessagesNavigationFragment"
    tools:layout="@layout/fragment_message" />
<fragment
    android:id="@+id/messageFragment"
    android:name="ifac.td.taxi.ui.screen.MessagesNavigationFragment"
    android:label="MessageFragment"
    tools:layout="@layout/fragment_message" />
MessagesNavigationFragmentDirections
    .actionMessagesNavigationFragmentToMessageDetailFragment(...)
requires a Safe Args action in your navigation XML for `MessagesNavigationFragment`.
<action
    android:id="@+id/goToMessageDetailFragment"
    app:destination="@id/messageDetailFragment" />
then you should instead define the action under the fragment destination you are using, for example:
<fragment
    android:id="@+id/messagesNavigationFragment"
    android:name="ifac.td.taxi.ui.screen.MessagesNavigationFragment"
    android:label="MessagesNavigationFragment">
    <action
        android:id="@+id/action_messagesNavigationFragment_to_messageDetailFragment"
        app:destination="@id/messageDetailFragment" />
</fragment>
onNavigateToMessageDetail = { messageId, skipAutoClose ->
    val bundle = Bundle().apply {
        putLong("messageId", messageId)
        putBoolean("skipAutoClose", skipAutoClose)
    }
    findNavController().navigate(R.id.messageDetailFragment, bundle)
}
