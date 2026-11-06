package ifac.td.taxi.ui.screen

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ifac.td.taxi.R
import ifac.td.taxi.databinding.FragmentSecurePinBinding
import ifac.td.taxi.ui.BaseFragment
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.custom.dialog.CustomDialog
import ifac.td.taxi.ui.custom.fields.FieldType
import ifac.td.taxi.viewmodel.SecurePinViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SecurePinFragment :
    BaseFragment<FragmentSecurePinBinding, SecurePinViewModel>(R.layout.fragment_secure_pin) {

    private val TAG = "SecurePinFragment"
    private val vModel: SecurePinViewModel by viewModel()

    override fun getViewModel(): SecurePinViewModel = vModel

    override fun getViewBinding() = FragmentSecurePinBinding.inflate(layoutInflater)

    override fun setupComponents() {
        iMainActivity.showBottomBar(true)
        iMainActivity.showHeader(true)

        setButtons()
        vModel.checkSecurePin()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vModel.hasSecurePinFlow.collect { hasSecurePin ->
                        hasSecurePin?.let { setupMode(it) }
                    }
                }
            }
        }
    }

    private fun setupMode(hasSecurePin: Boolean) {
        // Si ya hay un pin guardado la pantalla pasa a modo actualizar
        vBinding.apply {
            tvTitlePin.text = getString(
                if (hasSecurePin) R.string.title_update_pin else R.string.title_insert_pin
            )
            tvTitlePinRepeat.text = getString(
                if (hasSecurePin) R.string.title_update_repeat_pin else R.string.title_insert_repeat_pin
            )
        }
    }

    private fun setButtons() {
        vBinding.apply {
            btnAccept.setAction {
                clickOnAccept()
            }

            btnCancel.setAction {
                hideKeyboard()
                iMainActivity.navigateBack()
            }
        }
    }

    private fun clickOnAccept() {
        vBinding.apply {
            val pin = edPin.text.toString()
            val pinRepeat = edPinRepeat.text.toString()

            // Dejar ambos campos vacíos elimina el pin (guarda "")
            if (pin.isEmpty() && pinRepeat.isEmpty()) {
                savePin("")
                return
            }

            if (edPin.isValid(FieldType.PASSWORD) &&
                edPinRepeat.isValid(FieldType.REPEAT_PASSWORD, pin)
            ) {
                savePin(pin)
            }
        }
    }

    private fun savePin(pin: String) {
        hideKeyboard()
        vModel.insertSecurePin(pin)

        val callback: (CustomDialog.CustomDialogResponse) -> Unit = {
            iMainActivity.navigateBack()
        }

        iMainActivity.openDialog(
            CustomDialog.CustomDialogModel(
                title = getString(R.string.dialog_success),
                description = null,
                buttons = arrayListOf(ButtonType.ACCEPT)
            ),
            callback,
            fragmentManager = childFragmentManager,
        )
    }
}
