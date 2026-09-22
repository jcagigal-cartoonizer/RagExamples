package ifac.td.taxi.ui.custom.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.TTSType
import ifac.td.taxi.domain.usecase.TTSUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CustomDialogViewModel : ViewModel(), KoinComponent {

    var model: CustomDialog.CustomDialogModel? = null
    var onDismissFunction: ((CustomDialog.CustomDialogResponse) -> Unit)? = null

    private val ttsUseCase: TTSUseCase by inject()
    fun speakTTS(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            ttsUseCase.speakText(text, TTSType.Dialog)
        }
    }

    fun stopTTS() {
        viewModelScope.launch(Dispatchers.IO) {
            ttsUseCase.cancelCurrentSpeech()
        }
    }

}