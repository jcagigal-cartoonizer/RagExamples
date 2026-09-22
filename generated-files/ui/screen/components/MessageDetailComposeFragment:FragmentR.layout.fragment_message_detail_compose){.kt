package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 564-6: import androidx.compose.ui.platform.ComposeView
// import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ifac.td.taxi.ui.screen.compose.MessageDetailRoute
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
class MessageDetailComposeFragment : Fragment(R.layout.fragment_message_detail_compose) {
    private val vModel: ifac.td.taxi.viewmodel.MessageDetailComposeViewModel by viewModel()
    private val sharedViewModel: ifac.td.taxi.viewmodel.MainActivityViewModel by activityViewModel()
    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        val args = MessageDetailFragmentArgs.fromBundle(requireArguments())
        view.findViewById<ComposeView>(R.id.composeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MessageDetailRoute(
                    navController = findNavController(),
                    viewModel = vModel,
                    sharedViewModel = sharedViewModel,
                    messageId = args.messageId,
                    skipAutoClose = args.skipAutoClose,
                    onNavigateToPredefinedMessages = { messageId ->
                        findNavController().navigate(
                            MessageDetailComposeFragmentDirections.actionMessageDetailFragmentToPredefinedMessageFragment(
                                messageId,
                                true
                            )
                        )
                    }
                )
            }
        }
    }
}
`fragment_message_detail_compose.xml` can be as simple as:
<?xml version="1.0" encoding="utf-8"?>
<androidx.compose.ui.platform.ComposeView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/composeView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
The code above is intentionally close to the fragment logic. In a production Compose migration, you would likely also want:
