package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.domain.usecase.InformationMessagesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.AlfaMessageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InformationMessageViewModel(
    private val alfaMessageHandler: AlfaMessageHandler,
    private val informationMessageUseCase: InformationMessagesUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "InformationMessageViewModel"

    private val _informationMessageFlow = MutableStateFlow<List<String>?>(null)
    val informationMessageFlow = _informationMessageFlow.asStateFlow()

    private val _selectedInformationMessageFlow = MutableStateFlow<Pair<Int, String>?>(null)
    val selectedInformationMessageFlow = _selectedInformationMessageFlow.asStateFlow()


    fun initVM() {
        viewModelScope.launch(Dispatchers.IO) {
            val informationMessages = informationMessageUseCase.getInformationMessages().toList()
            Logs.d(TAG, "initVM: $informationMessages")
            _informationMessageFlow.emit(informationMessages)
        }
    }

    fun sendInformationMessage(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "sendInformationMessage: $id")
            alfaMessageHandler.requestInformation(id, 0, 0)
        }
    }

    fun changeSelectedInformationMessage(msg: Pair<Int, String>) {
        viewModelScope.launch{
            _selectedInformationMessageFlow.emit(msg)
        }
    }
}