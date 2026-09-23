package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToolsViewModel(
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    private val TAG = "ToolsViewModel"

    private val _meetingSignColors = MutableStateFlow<Pair<Int, Int>>(Pair(0,0))
    val meetingSignColors = _meetingSignColors.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                _meetingSignColors.emit(Pair(it.meetingSignTextColor, it.meetingSignBackgroundColor))
            }
        }
    }


    fun clickRequirements() {
        viewModelScope.launch {
            navigateTo(R.id.action_toolsFragment_to_requirementsFragment)
        }
    }
}