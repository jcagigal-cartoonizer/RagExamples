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
