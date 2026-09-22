package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.MeetingSignUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeetingSignViewModel(
    context: Application,
    private val meetingSignUseCase: MeetingSignUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
) : BaseViewModel(context) {

    private val TAG = "MeetingSignViewModel"

    private val _meetingSignNameFlow = MutableStateFlow<String?>(null)
    val meetingSignNameFlow = _meetingSignNameFlow.asStateFlow()

    init {
        viewModelScope.launch {
            _meetingSignNameFlow.emit(meetingSignUseCase.getMessageSignName())
        }
    }

    fun changeMessageSignText(text: String) {
        viewModelScope.launch {
            _meetingSignNameFlow.emit(text)
            meetingSignUseCase.setMessageSignText(text)
        }
    }

    fun checkShiftStatusChangeToHired(lastStatus: Int?, currentStatus: Int?): Boolean {
        lastStatus?.let { lstStatus ->
            currentStatus?.let { crntStatus ->
                Logs.d(TAG, "check change to hired last status $lstStatus , currentStatus $currentStatus")
                return shiftStatusUseCase.isVacant(lstStatus) && shiftStatusUseCase.isHired(crntStatus)
            }
        }

        return false
    }
}
