package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.usecase.BravoRestApiUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.rest.bravoRest.RequirementsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequirementsViewModel(context: Application, private val bravoRestApiUseCase: BravoRestApiUseCase) : BaseViewModel(context) {
    private val TAG = "RequirementsViewModel"

    private val _requirementsFlow = MutableStateFlow<RequirementsModel?>(null)
    val requirementsFlow = _requirementsFlow.asStateFlow()

    fun getRequirements() {
        viewModelScope.launch (Dispatchers.IO) {
            bravoRestApiUseCase.getCarsAndDriverRequirements { requirements ->
                Logs.d(TAG, "getRequirements: $requirements")
                _requirementsFlow.emit(requirements)
            }
        }
    }
}