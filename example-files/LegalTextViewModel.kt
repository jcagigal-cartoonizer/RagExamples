package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.R
import ifac.td.taxi.framework.sdk.usecase.ShowLegalTextUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LegalTextViewModel(
    context: Application,
    private val showLegalTextUseCase: ShowLegalTextUseCase,
) : BaseViewModel(context) {

    private val _legalTextFlow = MutableStateFlow<String?>(null)
    val legalTextFlow = _legalTextFlow.asStateFlow()

    fun clickOnAccept() {
        viewModelScope.launch {
            navigateTo(R.id.action_legalTextFragment_to_welcomeFragment)
        }
    }

    fun setLegalText() {
        viewModelScope.launch {
            val legalText = showLegalTextUseCase.getLegalText()
            _legalTextFlow.emit(legalText)
        }
    }
}
