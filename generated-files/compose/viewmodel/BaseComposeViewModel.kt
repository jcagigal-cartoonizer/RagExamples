package ifac.td.taxi.compose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class BaseComposeViewModel(
    application: Application
) : AndroidViewModel(application) {

    protected val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow.asStateFlow()

    protected val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    protected val _navigationFlow = MutableSharedFlow<UiNavigationEvent>()
    val navigationFlow = _navigationFlow.asSharedFlow()

    protected val _backPressedFlow = MutableSharedFlow<Unit>()
    val backPressedFlow = _backPressedFlow.asSharedFlow()

    fun setLoading(value: Boolean) {
        viewModelScope.launch {
            _loadingFlow.emit(value)
        }
    }

    fun showMessage(message: String) {
        viewModelScope.launch {
            _snackbarFlow.emit(message)
        }
    }

    fun navigateToRoute(route: String) {
        viewModelScope.launch {
            _navigationFlow.emit(UiNavigationEvent.ToRoute(route))
        }
    }

    fun navigateToId(id: Int) {
        viewModelScope.launch {
            _navigationFlow.emit(UiNavigationEvent.ToId(id))
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navigationFlow.emit(UiNavigationEvent.Back)
        }
    }

    fun navigateToDeepLink(uri: String) {
        viewModelScope.launch {
            _navigationFlow.emit(UiNavigationEvent.ToDeepLink(uri))
        }
    }
}
sealed class UiNavigationEvent {
    data class ToRoute(val route: String) : UiNavigationEvent()
    data class ToId(val id: Int) : UiNavigationEvent()
    data object Back : UiNavigationEvent()
    data class ToDeepLink(val uri: String) : UiNavigationEvent()
}