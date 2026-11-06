package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.taximeter.events.TotalizersRetrievedEvent
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TotalizersViewModel(context: Application, private val ticketUseCase: TicketUseCase) : BaseViewModel(context) {

    private val _totalizersStringFlow = MutableStateFlow<String?>(null)
    val totalizersStringFlow = _totalizersStringFlow.asStateFlow()

    fun loadTotalizersTicket(taximeterTotalizersFlow: TotalizersRetrievedEvent?) {
        viewModelScope.launch {
            if (taximeterTotalizersFlow == null) {
                _totalizersStringFlow.emit(null)
            } else {
                _totalizersStringFlow.emit(ticketUseCase.prepareTotalizersTicket(taximeterTotalizersFlow))
            }
        }
    }

}