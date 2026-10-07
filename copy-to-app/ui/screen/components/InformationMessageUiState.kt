package ifac.td.taxi.ui.screen.components
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
import ifac.td.taxi.ui.screen.InformationMessageScreen
// # Block 126-2: import android.app.Application
data class InformationMessageUiState(
    val informationMessages: List<String> = emptyList(),
    val selectedInformationMessage: Pair<Int, String>? = null,
    val dialogVisible: Boolean = false,
    val dialogTitle: String = "",
    val isLoading: Boolean = false
)
sealed interface InformationMessageUiEffect {
    data object NavigateBack : InformationMessageUiEffect
    data object NavigateBackAfterSend : InformationMessageUiEffect
    data class ShowToast(val messageRes: Int) : InformationMessageUiEffect
}
class InformationMessageComposeViewModel(
    private val alfaMessageHandler: AlfaMessageHandler,
    private val informationMessageUseCase: InformationMessagesUseCase,
    private val context: Application
) : ViewModel() {
    private val TAG = "InformationMessageComposeVM"
    lateinit var buttonsState : InformationMessageButtonsState
    lateinit var dialogState : InformationMessageDialogState
    private val _uiState = MutableStateFlow(InformationMessageUiState())
    val uiState = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<InformationMessageUiEffect>()
    val effects: SharedFlow<InformationMessageUiEffect> = _effects.asSharedFlow()
    fun initVM() {
        if (_uiState.value.informationMessages.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val informationMessages = informationMessageUseCase.getInformationMessages().toList()
            Logs.d(TAG, "initVM: $informationMessages")
            _uiState.update {
                it.copy(informationMessages = informationMessages)
            }
        }
    }
    fun onMessageSelected(index: Int, message: String) {
        _uiState.update {
            it.copy(
                selectedInformationMessage = index to message,
                dialogVisible = true,
                dialogTitle = context.getString(R.string.request_information)
            )
        }
    }
    fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                dialogVisible = false,
                selectedInformationMessage = null
            )
        }
    }
    fun onDialogButtonPressed(button: InformationMessageCustomButtonStyleDialogButtonType, messageId: Int) {
        when (button) {
            InformationMessageDialogButtonType.CANCEL -> onDialogDismiss()
            InformationMessageDialogButtonType.ACCEPT -> {
                viewModelScope.launch(Dispatchers.IO) {
                    Logs.d(TAG, "sendInformationMessage: $messageId")
                    alfaMessageHandler.requestInformation(messageId, 0, 0)
                    _effects.emit(InformationMessageUiEffect.ShowToast(R.string.datos_enviados))
                    _effects.emit(InformationMessageUiEffect.NavigateBackAfterSend)
                    _uiState.update { it.copy(dialogVisible = false, selectedInformationMessage = null) }
                }
            }
        }
    }
    fun onCancelPressed() {
        viewModelScope.launch {
            _effects.emit(InformationMessageUiEffect.NavigateBack)
        }
    }
}
enum class InformationMessageCustomButtonStyleDialogButtonType {
    CANCEL,
    ACCEPT
}
