package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 526-8: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun CropImageCustomDialog(
    title: String,
    description: String,
    confirmText: String,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissText != null) {
                        TextButton(onClick = onDismiss) {
                            Text(text = dismissText)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text(text = confirmText)
                    }
                }
            }
        }
    }
}
In your old fragment, navigation/UI actions were delegated to `iMainActivity`.  
In Compose, preserve that by passing callbacks from the host:
So your host Activity/Fragment can keep the same navigation behavior and permission flow.
class CropImageComposeFragment : Fragment() {
    private val viewModel: CropImageViewModel by viewModel()
    private val navController by lazy { findNavController() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CropImageScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onRequestHeaderVisibility = { visible ->
                        (activity as? YourMainActivity)?.showHeader(visible)
                    },
                    onRequestCameraPermission = {
                        (activity as? YourMainActivity)?.requestPermission(
                            PermissionRequest.PermissionTypeList.PERMISSION_CAMERA
                        )
                    },
                    onOpenCropImage = { uri ->
                        (activity as? YourMainActivity)?.openCropImage(uri)
                    },
                    onOpenImageSelect = {
                        (activity as? YourMainActivity)?.openImageSelect()
                    },
                    onShowToast = { resId ->
                        (activity as? YourMainActivity)?.showToast(resId)
                    }
                )
            }
        }
    }
}
1. a **full integration with your `MainActivityViewModel`** equivalent in Compose,
2. a **more exact Material 2 / XML-style button design** based on likely `CustomButton`,
3. a version using **Navigation Compose routes** instead of callback-based host navigation.
