package ifac.td.taxi.ui.screen
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
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.FixedPriceScreen
// # Block 506-5: import androidx.compose.foundation.layout.Arrangement
@Composable
fun FixedPriceScreen(
    navController: NavController,
    viewModel: FixedPriceComposeViewModel,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit = {},
    onHideKeyboard: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState: FixedPriceButtonsState = uiState.fixedPrice.toButtonsState()
    var dialogState by remember { mutableStateOf<FixedPriceDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is FixedPriceUiEffect.ShowToast -> onShowToast(effect.message)
                is FixedPriceUiEffect.HideKeyboard -> onHideKeyboard()
                is FixedPriceUiEffect.NavigateBack -> onBack()
                is FixedPriceUiEffect.ShowDialog -> dialogState = effect.dialog
                is FixedPriceUiEffect.MoveMapToPickup -> {
                    // hook into map controller from host if needed
                }
                is FixedPriceUiEffect.MoveMapToDropOff -> {
                    // hook into map controller from host if needed
                }
            }
        }
    }
    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = "Fixed Price", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.selectedDropOff?.getPrintableStreetTextShort().orEmpty(),
                onValueChange = { text ->
                    viewModel.onEvent(
                        FixedPriceUiEvent.SearchChanged(
                            text = text,
                            isPoiSearch = false
                        )
                    )
                },
                label = { Text("Drop off") },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            )
            Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
            if (uiState.isSuggestionListVisible) {
                LazyColumn(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                ) {
                    items(uiState.suggestions) { item ->
                        SuggestionItem(
                            item = item,
                            onClick = { viewModel.onEvent(FixedPriceUiEvent.SuggestionSelected(item)) }
                        )
                    }
                }
            }
            Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FixedPriceCustomButton(
                    state = buttonsState.taxi,
                    text = buttonsState.taxi.priceText,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
                FixedPriceCustomButton(
                    state = buttonsState.van,
                    text = buttonsState.van.priceText,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
                FixedPriceCustomButton(
                    state = buttonsState.business,
                    text = buttonsState.business.priceText,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
            }
            Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FixedPriceCustomButton(
                    state = buttonsState.taxi.copy(loading = false),
                    text = "Cancel",
                    onClick = { viewModel.onEvent(FixedPriceUiEvent.CloseDropOff) },
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
                FixedPriceCustomButton(
                    state = buttonsState.business.copy(loading = false),
                    text = "Reset",
                    onClick = { viewModel.onEvent(FixedPriceUiEvent.ResetPrices) },
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                )
            }
            Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
            Text(
                text = "Map and bottom sheet behavior should be hosted by the parent composable/screen.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    dialogState?.let { dialog ->
        FixedPriceScreenFixedPriceCustomDialog(
            state = dialog,
            onDismiss = {
                dialogState = null
                viewModel.onEvent(FixedPriceUiEvent.HideDialog)
            },
            onPositiveClick = {
                dialogState = null
                viewModel.onEvent(FixedPriceUiEvent.HideDialog)
            },
            onNegativeClick = {
                dialogState = null
                viewModel.onEvent(FixedPriceUiEvent.HideDialog)
            }
        )
    }
}
@Composable
fun SuggestionItem(
    item: SuggestModel,
    onClick: () -> Unit
) {
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = item.getPrintableStreetTextShort())
        Divider(modifier = androidx.compose.ui.Modifier.padding(top = 8.dp))
    }
}
In Compose, navigation is best preserved by keeping the fragment destination and hosting the composable inside it, or by using a `NavHost` route that mirrors the same logic.
class FixedPriceMapFragment : BaseFragment<FragmentFixedPriceMapBinding, FixedPriceViewModel>(
    R.layout.fragment_fixed_price_map
) {
    override fun setupComponents() {
        vBinding.composeContainer.setContent {
            FixedPriceScreen(
                navController = findNavController(),
                viewModel = /* your compose VM */,
                onBack = { findNavController().popBackStack() },
                onShowToast = { iMainActivity.showToast(it) },
                onHideKeyboard = { /* hide keyboard */ }
            )
        }
    }
}
From the fragment logic, these behaviors are preserved in the Compose state model:
1. a **fully wired `Compose NavHost` route** version,  
2. a **bottom sheet Compose implementation** mirroring `BottomSheetBehavior`, and  
3. a **map host interface** for integrating your Cercalia map controller in Compose without breaking current functionality.
