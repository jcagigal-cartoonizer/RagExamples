package ifac.td.taxi.compose.viewmodel
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
import ifac.td.taxi.viewmodel.MessagesViewModel
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
import ifac.td.taxi.ui.screen.MessagesScreen
// # Block 56-2: import android.app.Application
class MessagesComposeViewModel(
    private val messageUseCase: MessageUseCase,
    context: Application,
) : BaseViewModel(context) {
    fun fromViewModel(viewModel: MessagesViewModel): MessagesComposeViewModel {
        val composeViewModel = MessagesComposeViewModel(
            context = viewModel.context,
            messageUseCase = viewModel.messageUseCase,
        )
        return composeViewModel
    }

    private val TAG = "MessagesViewModel"
    lateinit var buttonsState : MessagesButtonsState
    lateinit var dialogState : MessagesDialogState
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<MessagesUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()
    fun init() {
        loadMessagesByDriverId()
    }
    fun onEvent(event: MessagesUiEvent) {
        when (event) {
            MessagesUiEvent.Back -> {
viewModelScope.launch { emitEffect(MessagesUiEffect.NavigateBack)
}
}
            MessagesUiEvent.DeleteSelected -> deleteSelectedMessages()
            MessagesUiEvent.ToggleSelectionMode -> {
                _uiState.update { state ->
                    val newMode = !state.isSelectionMode
                    state.copy(
                        isSelectionMode = newMode,
                        selectedMessageIds = if (newMode) state.selectedMessageIds else emptySet(),
                        showDeleteButton = newMode,
                        showSelectButton = true
                    )
                }
            }
            MessagesUiEvent.MarkAllReadRequest -> {
                _uiState.update {
                    it.copy(
                        dialogState = MessageMessagesDialogState(
                            title = "Warning",
                            description = "Do you want to mark all messages as read?",
                            buttons = listOf(DialogButtonConfig.Cancel, DialogButtonConfig.Accept)
                        )
                    )
                }
                emitEffect(MessagesUiEffect.ShowMarkAllReadDialog)
            }
            MessagesUiEvent.MarkAllReadConfirmed -> {
                _uiState.update { it.copy(dialogState = null) }
                markAllMessagesRead()
            }
            MessagesUiEvent.DismissDialog -> {
                _uiState.update { it.copy(dialogState = null) }
            }
            is MessagesUiEvent.MessageClicked -> {
                emitEffect(
                    MessagesUiEffect.NavigateToMessageDetailWithArgs(
                        messageId = event.message.id,
                        skipAutoClose = true
                    )
                )
            }
            is MessagesUiEvent.ToggleMessageSelection -> {
                _uiState.update { state ->
                    val id = event.message.id
                    val newSelection = state.selectedMessageIds.toMutableSet().apply {
                        if (!add(id)) remove(id)
                    }
                    state.copy(
                        selectedMessageIds = newSelection,
                        showDeleteButton = newSelection.isNotEmpty()
                    )
                }
            }
        }
    }
    fun deleteSelectedMessages() {
        val selectedIds = _uiState.value.selectedMessageIds
        if (selectedIds.isEmpty()) return
        val selectedMessages = _uiState.value.messages.filter { it.id in selectedIds }.toMutableList()
        deleteMessages(selectedMessages)
        _uiState.update {
            it.copy(
                selectedMessageIds = emptySet(),
                isSelectionMode = false,
                showDeleteButton = false
            )
        }
    }
    fun emitEffect(effect: MessagesUiEffect) {
        viewModelScope.launch { _uiEffects.emit(effect) }
    }
    fun deleteMessages(deleteSelected: MutableList<MessageEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            messageUseCase.deleteMessages(deleteSelected)
            loadMessagesByDriverId()
        }
    }
    fun loadMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            _uiState.update {
                it.copy(
                    messages = messageUseCase.getMessages(),
                    isLoading = false
                )
            }
        }
    }
    fun loadMessagesByDriverId() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "loadMessagesByDriverId: getMessageByDriverId(): ${messageUseCase.getMessageByDriverId()}")
            _uiState.update { it.copy(isLoading = true) }
            _uiState.update {
                it.copy(
                    messages = messageUseCase.getMessageByDriverId(),
                    isLoading = false
                )
            }
        }
    }
    fun markAllMessagesRead() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "markAllMessagesRead")
            messageUseCase.setAllMessagesRead()
            _uiState.update {
                it.copy(messages = messageUseCase.getMessageByDriverId())
            }
        }
    }
}
